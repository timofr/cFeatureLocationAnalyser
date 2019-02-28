package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;
import parser.pattern.CloseCurlyBracketPattern;
import parser.pattern.CppDefinePattern;
import parser.pattern.CppElifPattern;
import parser.pattern.CppElsePattern;
import parser.pattern.CppEndifPattern;
import parser.pattern.CppIfDefinedPattern;
import parser.pattern.CppIfdefPattern;
import parser.pattern.CppIfndefPattern;
import parser.pattern.FunctionDefinitionPattern;
import parser.pattern.OpenCurlyBracketPattern;
import parser.pattern.Pattern;

public class Parser {
	
	private int lineCounter = 1;

	private final Lexer lexer;
	
	private Token lookahead;
	
	private final Set<PatternMatcher> matchers = new LinkedHashSet<PatternMatcher>();
	
	private Map<Class<? extends Pattern>, Consumer<PatternOccurance>> handlePattern = 
			new HashMap<Class<? extends Pattern>, Consumer<PatternOccurance>>();
	
	private final List<PatternOccurance> occurances = new ArrayList<PatternOccurance>();
	
	private List<FunctionDefinition> functions = new ArrayList<FunctionDefinition>();
	private List<Ifdef> ifdefs = new ArrayList<Ifdef>();
	
	private Map<FunctionDefinition, List<Ifdef>> analysedFunctions = new HashMap<FunctionDefinition, List<Ifdef>>();
	
	private List<FunctionDefinition> functionsToAnalyse = new ArrayList<FunctionDefinition>();
	private Stack<Ifdef> ifdefStack = new Stack<Ifdef>();
	private Stack<Integer> bracketStack = new Stack<Integer>();

	public Parser(Lexer lexer) {
		this.lexer = lexer;
		initialize();
	}
	
	public List<FunctionDefinition> getFunctions() {
		return functions;
	}

	public List<Ifdef> getIfdefs() {
		return ifdefs;
	}
	
	public Map<FunctionDefinition, List<Ifdef>> getAnalysedFunctions() {
		return analysedFunctions;
	}
	
	public List<PatternOccurance> getOccurances() {
		return occurances;
	}
	
	private void initialize() {
		//ifdefStack.push(new Ifdef("CFILE", 1));
		
		bracketStack.push(0);
		
		this.matchers.add(PatternMatcher.getCppDefinePatternMatcher());
		this.matchers.add(PatternMatcher.getCppIfdefPatternMatcher());
		this.matchers.add(PatternMatcher.getCppIfDefinedPatternMatcher());
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
		this.handlePattern.put(CppDefinePattern.class, this::handleNothing);
		this.handlePattern.put(CppElifPattern.class, this::throwException);
		this.handlePattern.put(CppIfndefPattern.class, this::throwException); 
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
	
	private void parse() throws LexerException {
		this.nextToken();
		
		while(lookahead.getType() != TokenType.EOF) {
			
			if(lookahead.getType() == TokenType.NEWLINE)
				this.lineCounter++;
			
			for(PatternMatcher m : matchers) 
				this.occurances.addAll(m.match(lookahead, lineCounter));
			
			this.nextToken();
		}
		
		//ifdefStack.peek().setEndLine(lineCounter);  // only used if cfile ifdef is used
		
		for(PatternMatcher m : matchers) 
			this.occurances.addAll(m.match(lookahead, lineCounter));
	}
	
	int lastBracket = 0; //avoid inner class access error
	
	private void determineRanges() throws LexerException {
		this.parse();
		
		for(PatternOccurance po : occurances) {
			handlePattern.get(po.getPattern().getClass()).accept(po);
		}
			
		assert bracketStack.peek() == 0 : "Last function got more '{' than '}'";
		functionsToAnalyse.forEach(f -> f.setEnd(lastBracket));
		functions.addAll(functionsToAnalyse);
		functionsToAnalyse.clear();
	}
	
	public Map<FunctionDefinition, List<Ifdef>> analyse() throws LexerException {
		determineRanges();
		
		functions.stream()
			.filter(f -> f.getIfdef().stream().map(id -> id.isIfdef()).reduce(true, Boolean::logicalAnd))
			.forEach(f -> analysedFunctions.put(f, new ArrayList<Ifdef>()));
		
		for(Ifdef i : ifdefs) {
			for(FunctionDefinition f : functions) {
				if(i.getElseLine() == -1) {
					if((f.getStart() > i.getStartLine()) && (f.getEnd() < i.getEndLine()))
						analysedFunctions.get(f).add(i);
				} else {
					if((f.getStart() > i.getStartLine()) && (f.getEnd() < i.getElseLine()))
						analysedFunctions.get(f).add(i);
				}
			}
		}
		
		return analysedFunctions;
	}
	

	
	private void handleFunctionDefinitionPattern(PatternOccurance po) {
		if(bracketStack.stream().reduce((a,b) -> a | b).get() == 0) {
			functionsToAnalyse.forEach(f -> f.setEnd(lastBracket));
			functions.addAll(functionsToAnalyse);
			functionsToAnalyse.clear();
		}
		
		functionsToAnalyse.add(new FunctionDefinition(
						po.getContent().get(1).getContent(),
						ifdefStack.stream().map(Ifdef::new).collect(Collectors.toList()), //deep copy of ifdef stack to list
						po.getStartLine()));
	}
	
	private void handleIfdefPattern(PatternOccurance po) {
		ifdefStack.push(new Ifdef(po.getContent().get(2).getContent(), po.getStartLine()));
		bracketStack.push(bracketStack.peek());
	}
	
	private void handleIfDefinedPattern(PatternOccurance po) {
		ifdefStack.push(new Ifdef(getIfDefinedIdentifier(po.getContent()), po.getStartLine()));
		bracketStack.push(bracketStack.peek());
	}

	private void handleElsePattern(PatternOccurance po) {
		ifdefStack.peek().setIfdef(false);
		ifdefStack.peek().setElseLine(po.getStartLine());
		bracketStack.push(popAndChangeTop(bracketStack));
	}

	private void handleEndifPattern(PatternOccurance po) {
		ifdefStack.peek().setEndLine(po.getEndLine());
		ifdefs.add(ifdefStack.pop());
		bracketStack.pop(); //no difference to popAndChangeTop(bracket) noticed
		//popAndChangeTop(bracket); //TODO check difference
	}
	
	public void handleOpenCurlyBracketPattern(PatternOccurance po) {
		changeTopOfStack(bracketStack, true);
	}
	
	public void handleCloseCurlyBracketPattern(PatternOccurance po) {
		changeTopOfStack(bracketStack, false);
		lastBracket = po.getEndLine();
	}
	
	public void handleNothing(PatternOccurance po) {}

	public void throwException(PatternOccurance po) {
		throw new RuntimeException("Cant handle " + po);
	}
}
