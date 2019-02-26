package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
import parser.pattern.CppElsePattern;
import parser.pattern.CppEndifPattern;
import parser.pattern.CppIfDefinedPattern;
import parser.pattern.CppIfdefPattern;
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
	
	private final List<PatternOccurance> occurances = new LinkedList<PatternOccurance>();
	
	private List<FunctionDefinitionOccurance> functions = new LinkedList<FunctionDefinitionOccurance>();
	
	private List<FunctionDefinitionOccurance> functionsToAnalyse = new ArrayList<FunctionDefinitionOccurance>();
	private Stack<Ifdef> ifdef = new Stack<Ifdef>();
	private Stack<Integer> bracket = new Stack<Integer>();
	
	public Parser(Lexer lexer) {
		this.lexer = lexer;
		initialize();
	}
	
	private void initialize() {
		ifdef.push(new Ifdef("CFILE", false));
		bracket.push(0);
		
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
		
		for(PatternMatcher m : matchers) 
			this.occurances.addAll(m.match(lookahead, lineCounter));
	}
	
	int lastBracket = 0; //avoid inner class access error
	
	public List<FunctionDefinitionOccurance> searchForFunctionRanges() throws LexerException {
		this.parse();
		
		for(PatternOccurance po : occurances) {
			handlePattern.get(po.getPattern().getClass()).accept(po);
		}
			
		assert bracket.peek() == 0 : "Last function got more '{' than '}'";
		functionsToAnalyse.forEach(f -> f.setEnd(lastBracket));
		functions.addAll(functionsToAnalyse);
		functionsToAnalyse.clear();
		
		return functions;
	}
	

	
	private void handleFunctionDefinitionPattern(PatternOccurance po) {
		if(bracket.stream().reduce((a,b) -> a | b).get() == 0) {
			functionsToAnalyse.forEach(f -> f.setEnd(lastBracket));
			functions.addAll(functionsToAnalyse);
			functionsToAnalyse.clear();
		}
		
		functionsToAnalyse.add(new FunctionDefinitionOccurance(
						po.getContent().get(1).getContent(),
						ifdef.stream().map(Ifdef::new).collect(Collectors.toList()), //deep copy of ifdef stack to list
						po.getStartLine()));
	}
	
	private void handleIfdefPattern(PatternOccurance po) {
		ifdef.push(new Ifdef(po.getContent().get(2).getContent(), true));
		bracket.push(bracket.peek());
	}
	
	private void handleIfDefinedPattern(PatternOccurance po) {
		ifdef.push(new Ifdef(getIfDefinedIdentifier(po.getContent()), true));
		bracket.push(bracket.peek());
	}

	private void handleElsePattern(PatternOccurance po) {
		ifdef.peek().setIfdef(false);
		bracket.push(popAndChangeTop(bracket));
	}

	private void handleEndifPattern(PatternOccurance po) {
		ifdef.pop();
		bracket.pop(); //no difference to popAndChangeTop(bracket) noticed
		//popAndChangeTop(bracket); //TODO check difference
	}
	
	public void handleOpenCurlyBracketPattern(PatternOccurance po) {
		changeTopOfStack(bracket, true);
	}
	
	public void handleCloseCurlyBracketPattern(PatternOccurance po) {
		changeTopOfStack(bracket, false);
	}
	
	public void handleNothing(PatternOccurance po) {}


}
