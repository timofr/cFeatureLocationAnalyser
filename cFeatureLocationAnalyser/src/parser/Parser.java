package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;
import main.DatabaseHandler;
import parser.pattern.CloseCurlyBracketPattern;
import parser.pattern.CppDefinePattern;
import parser.pattern.CppElifPattern;
import parser.pattern.CppElsePattern;
import parser.pattern.CppEndifPattern;
import parser.pattern.CppIfDefinedPattern;
import parser.pattern.CppIfdefPattern;
import parser.pattern.CppIfndefPattern;
import parser.pattern.CppUndefinePattern;
import parser.pattern.FunctionDefinitionPattern;
import parser.pattern.OpenCurlyBracketPattern;
import parser.pattern.Pattern;

public class Parser {
	
	private File file;
	

	private final Lexer lexer;
	
	private Token lookahead;
	
	private final Set<PatternMatcher> matchers = new LinkedHashSet<PatternMatcher>();
	
	private Map<Class<? extends Pattern>, Consumer<PatternOccurance>> handlePattern = 
			new HashMap<Class<? extends Pattern>, Consumer<PatternOccurance>>();
	
	private final List<PatternOccurance> occurances = new ArrayList<PatternOccurance>();
	
	private List<FunctionDefinition> functions = new ArrayList<FunctionDefinition>();
	private List<Ifdef> ifdefs = new ArrayList<Ifdef>();
	
	private Map<String, List<FunctionData>> tempAnalysedFunctions = new HashMap<String, List<FunctionData>>();
	private Map<String, List<FunctionData>> analysedFunctions = DatabaseHandler.getInstance().getAnalysedFunctions();
	
	private Map<String, Set<String>> tempTransitiveDefine = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> tempTransitiveUndefine = new HashMap<String, Set<String>>();
	
	private Map<String, Set<String>> transitiveDefine = DatabaseHandler.getInstance().getTransitiveDefine();
	private Map<String, Set<String>> transitiveUndefine = DatabaseHandler.getInstance().getTransitiveUndefine();
	
	private List<FunctionDefinition> functionsToAnalyse = new ArrayList<FunctionDefinition>();
	private Stack<Ifdef> ifdefStack = new Stack<Ifdef>();
	private Stack<Integer> bracketStack = new Stack<Integer>();
	private int lastBracket = 0;
	
	public Parser(Lexer lexer) {
		this.lexer = lexer;
		this.file = lexer.getFile();
		initialize();
	}
	
	public File getFile() {
		return file;
	}
	
	public List<FunctionDefinition> getFunctions() {
		return functions;
	}

	public List<Ifdef> getIfdefs() {
		return ifdefs;
	}
	
	public Map<String, List<FunctionData>> getAnalysedFunctions() {
		return tempAnalysedFunctions;
	}
	
	public List<PatternOccurance> getOccurances() {
		return occurances;
	}
	
	private void initialize() {
		//ifdefStack.push(new Ifdef("CFILE", 1));
		
		bracketStack.push(0);
		
		this.matchers.add(PatternMatcher.getCppDefinePatternMatcher());
		this.matchers.add(PatternMatcher.getCppUndefinePatternMatcher());
		this.matchers.add(PatternMatcher.getCppIfdefPatternMatcher());
		this.matchers.add(PatternMatcher.getCppIfDefinedPatternMatcher());
		this.matchers.add(PatternMatcher.getCppIfndefPatternMatcher());
		this.matchers.add(PatternMatcher.getCppElsePatternMatcher());
		this.matchers.add(PatternMatcher.getCppElifPatternMatcher());
		this.matchers.add(PatternMatcher.getCppEndifPatternMatcher());
		this.matchers.add(PatternMatcher.getFunctionDefinitionPatternMatcher());
		this.matchers.add(PatternMatcher.getOpenCurlyBracketPatternMatcher());		
		this.matchers.add(PatternMatcher.getCloseCurlyBracketPatternMatcher());
		
		this.handlePattern.put(CppIfdefPattern.class, this::handleIfdefPattern);
		this.handlePattern.put(CppIfDefinedPattern.class, this::handleIfDefinedPattern);
		this.handlePattern.put(CppElsePattern.class, this::handleElsePattern);
		this.handlePattern.put(CppEndifPattern.class, this::handleEndifPattern);
		this.handlePattern.put(OpenCurlyBracketPattern.class, this::handleOpenCurlyBracketPattern);
		this.handlePattern.put(CloseCurlyBracketPattern.class, this::handleCloseCurlyBracketPattern);
		this.handlePattern.put(FunctionDefinitionPattern.class, this::handleFunctionDefinitionPattern);
		this.handlePattern.put(CppDefinePattern.class, this::handleDefinePattern);
		this.handlePattern.put(CppUndefinePattern.class, this::handleUndefinePattern);
		this.handlePattern.put(CppElifPattern.class, this::handleElifPattern);
		this.handlePattern.put(CppIfndefPattern.class, this::handleIfndefPattern); 
	}
	
	private void nextToken() throws LexerException {
		this.lookahead = lexer.nextToken();
	}
	
	private void changeTopOfStack(Stack<Integer> stack, boolean increment) {
		int top = stack.pop();
		stack.push(increment? ++top : --top);
	}
	
	private int popAndChangeTop(Stack<Integer> stack) {
		int top = stack.pop();
		int secondTop = stack.pop();
		stack.push(top);
		return secondTop;
	}
	
	private String getIfDefinedIdentifier(List<Token> tokens) {
		StringBuilder builder = new StringBuilder();
		tokens.remove(0);
		tokens.remove(0);
		for(Token t : tokens) {
			builder.append(t.getContent()).append(' ');
		}
		return builder.substring(0, builder.length() - 1);
	}
	
	public void parse() throws LexerException {
		this.nextToken();
		
		while(lexer.hasNext()) {
			matchPatterns();
			
			this.nextToken();
		}
		
		//ifdefStack.peek().setEndLine(lineCounter);  // only used if cfile ifdef is used
		
		matchPatterns();
	}

	private void matchPatterns() {
		for(PatternMatcher m : matchers) 
			this.occurances.addAll(m.match(lookahead));
	}
	
	public void determineRanges() throws ParserException {
		try {
			for(PatternOccurance po : occurances) {
				handlePattern.get(po.getPattern().getClass()).accept(po);
			}
		}
		catch(EmptyStackException e) {
			throw new ParserException("Some function or ifdef does not got closed properly."
					+ "\nBracketStack size: " + bracketStack.size()
					+ "\nBracketStack size: " + bracketStack.size());
		}
			
		if(bracketStack.peek() != 0) throw new ParserException("Some function does not get closed properly. BracketStack size: " + bracketStack.size());
		if(ifdefStack.size() != 0) throw new ParserException("Some Ifdef does not get closed properly. BracketStack size: " + bracketStack.size());
		functionsToAnalyse.forEach(f -> f.setEnd(lastBracket));
		functions.addAll(functionsToAnalyse);
		functionsToAnalyse.clear();
	}

	private void analyseRanges() {	
		for(FunctionDefinition f : functions) {
			if(tempAnalysedFunctions.get(f.getName()) == null) { //TODO extract this
				tempAnalysedFunctions.put(f.getName(), new ArrayList<FunctionData>());
			}
			
			List<Ifdef> posIfdef = new ArrayList<Ifdef>();
			List<Ifdef> negIfdef = new ArrayList<Ifdef>();
			
			for(Ifdef i : ifdefs) {
				if(i.isN()) {
					if (i.getElseLine() == -1) {
						if ((f.getStart() > i.getStartLine()) && (f.getEnd() < i.getEndLine()))
							negIfdef.add(i);
					} else {
						if ((f.getStart() > i.getStartLine()) && (f.getEnd() < i.getElseLine()))
							negIfdef.add(i);
						else if((f.getStart() > i.getElseLine()) && (f.getEnd() < i.getEndLine())) {
							posIfdef.add(i);
						}
					}
				}
				else {
					if (i.getElseLine() == -1) {
						if ((f.getStart() > i.getStartLine()) && (f.getEnd() < i.getEndLine()))
							posIfdef.add(i);
					} else {
						if ((f.getStart() > i.getStartLine()) && (f.getEnd() < i.getElseLine()))
							posIfdef.add(i);
						else if((f.getStart() > i.getElseLine()) && (f.getEnd() < i.getEndLine())) {
							negIfdef.add(i);
						}
					}
				}
			}
			
			tempAnalysedFunctions.get(f.getName()).add(new FunctionData(f, file, posIfdef, negIfdef));
		}
	}
	
	public void analyse() {
		try {
			this.parse();
			this.determineRanges();
			this.analyseRanges();
		}
		catch(LexerException le) {
			System.err.println("Failed to lexically analyse file " + file.getAbsolutePath());
			System.err.println("Please analyse it by hand");
			le.printStackTrace();
		}
		catch(ParserException pe) {
			this.overestimate();
			System.err.println("Failed to parse file " + file.getAbsolutePath());
			System.err.println("Overestimation: All ifdefs will be added.");
			pe.printStackTrace();
		}
		
		catch(Exception e) {
			System.err.println("Failed in file " + file.getAbsolutePath());
			System.err.println("Something unexpected happened.");
			e.printStackTrace();
		}
		
		this.addTempToDatabase(tempAnalysedFunctions, analysedFunctions);
		this.addTempToDatabase(tempTransitiveDefine, transitiveDefine);
		this.addTempToDatabase(tempTransitiveUndefine, transitiveUndefine);
	}
	
	private void overestimate() {
		functions.clear();
		ifdefs.clear();
		
		occurances.stream()
			.filter(o -> isIfdef(o))
			.map(o -> new Ifdef(o.getContent().get(2).getContent()))
			.forEach(i -> ifdefs.add(i)); //FIXME does not work for if and elif
		
		occurances.stream()
			.filter(o -> FunctionDefinitionPattern.class.isInstance(o))
			.map(o -> new FunctionDefinition(o.getContent().get(0).getContent(), ifdefs))
			.forEach(f -> functions.add(f));
	}
	
	private boolean isIfdef(PatternOccurance o) {
		return CppIfdefPattern.class.isInstance(o) || CppIfndefPattern.class.isInstance(o)
				|| CppIfDefinedPattern.class.isInstance(o) || CppElifPattern.class.isInstance(o);
	}

	private <T> void addTempToDatabase(Map<String, ? extends Collection<T>> temp, Map<String, ? extends Collection<T>> database) {
		for(Entry<String, ? extends Collection<T>> e: temp.entrySet()) {
			String name = e.getKey();
			Collection<T> newData = e.getValue();
			Collection<T> data = database.get(name);
			Map<String, Collection<T>> databaseNew  = (Map<String, Collection<T>>) database; 
			if(data == null) 
				databaseNew.put(name, newData);	
			else 
				data.addAll(newData);
		}
		
	}
	
//	private void <T, V> addTempToDatabase(Map<T, V> temp, Map<T, V> database) {
//		for(Entry<String, List<FunctionData>> e: tempAnalysedFunctions.entrySet()) {
//			String name = e.getKey();
//			List<FunctionData> newData = e.getValue();
//			List<FunctionData> data = analysedFunctions.get(name);
//			if(data == null) 
//				analysedFunctions.put(name, newData);	
//			else 
//				data.addAll(newData);
//		}
//		
//	}

	private void handleFunctionDefinitionPattern(PatternOccurance po) {
		if(bracketStack.stream().reduce((a,b) -> a | b).get() == 0) {
			functionsToAnalyse.forEach(f -> f.setEnd(lastBracket));
			functions.addAll(functionsToAnalyse);
			functionsToAnalyse.clear();
		}
		
		functionsToAnalyse.add(new FunctionDefinition(
						po.getContent().get(0).getContent(),
						ifdefStack.stream().map(Ifdef::new).collect(Collectors.toList()), //deep copy of ifdef stack to list
						po.getStartLine()));
	}
	
	private void handleIfdefPattern(PatternOccurance po) {
		ifdefStack.push(new Ifdef(po.getContent().get(2).getContent(), true, po.getStartLine()));
		bracketStack.push(bracketStack.peek());
	}
	
	private void handleIfDefinedPattern(PatternOccurance po) {
		ifdefStack.push(new Ifdef(getIfDefinedIdentifier(po.getContent()), true, po.getStartLine()));
		bracketStack.push(bracketStack.peek());
	}

	private void handleIfndefPattern(PatternOccurance po) {
		Ifdef i = new Ifdef(po.getContent().get(2).getContent(), false, po.getStartLine());
		i.setElseLine(po.getEndLine());
		ifdefStack.push(i);
		
		bracketStack.push(bracketStack.peek());
	}
	
	private void handleElsePattern(PatternOccurance po) {
		Ifdef top = ifdefStack.peek();
		if(top.isN()) {
			top.setIfdef(true);
			top.setElseLine(po.getStartLine());
			bracketStack.push(popAndChangeTop(bracketStack));
		}
		else {
			top.setIfdef(false);
			top.setElseLine(po.getStartLine());
			bracketStack.push(popAndChangeTop(bracketStack));
		}
	}
	
	private void handleElifPattern(PatternOccurance po) { //FIXME creates inconsistency
		handleEndifPattern(po);
		handleIfDefinedPattern(po);
	}

	private void handleEndifPattern(PatternOccurance po) {
		ifdefStack.peek().setEndLine(po.getEndLine());
		ifdefs.add(ifdefStack.pop());
		//bracketStack.pop(); //no difference to popAndChangeTop(bracket) noticed
		popAndChangeTop(bracketStack); //PRECARIOUS check difference to bracketStack.pop()
	}
	
	private void handleOpenCurlyBracketPattern(PatternOccurance po) {
		changeTopOfStack(bracketStack, true);
	}
	
	private void handleCloseCurlyBracketPattern(PatternOccurance po) {
		changeTopOfStack(bracketStack, false);
		lastBracket = po.getEndLine();
	}
	
	private void handleDefinePattern(PatternOccurance po) {
		String s = po.getContent().get(2).getContent();
		ifdefStack.forEach(i -> {
			i.addDefine(s);
			Set<String> set = tempTransitiveDefine.get(s);
			if(set == null) {
				set = new HashSet<String>();
				tempTransitiveDefine.put(s, set);
			}
			set.add(i.getName());
		});
		
	}
	
	private void handleUndefinePattern(PatternOccurance po) {
		String s = po.getContent().get(2).getContent();
		ifdefStack.forEach(i -> {
			i.addUndefine(s);
			Set<String> set = tempTransitiveDefine.get(s);
			if(set == null) {
				set = new HashSet<String>();
				tempTransitiveUndefine.put(s, set);
			}
			set.add(i.getName());
		});
	}
	
	//public void handleNothing(PatternOccurance po) {}
}
