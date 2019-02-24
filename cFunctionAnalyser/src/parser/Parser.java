package parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collector;
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
import parser.pattern.CppIfdefPattern;
import parser.pattern.FunctionDefinitionPattern;
import parser.pattern.OpenCurlyBracketPattern;
import parser.pattern.Pattern;

public class Parser {
	
	private int lineCounter = 1;

	private final Lexer lexer;
	
	private Token lookahead;
	
	private final Set<PatternMatcher> matchers = new LinkedHashSet<PatternMatcher>();
	
	private final List<PatternOccurance> occurances = new LinkedList<PatternOccurance>();
	
	private List<FunctionDefinitionOccurance> functions = new LinkedList<FunctionDefinitionOccurance>();
	
	public Parser(Lexer lexer) {
		this.lexer = lexer;
		this.initialize();
	}
	
	private void initialize() {
		this.matchers.add(new PatternMatcher(new CppDefinePattern()));
		this.matchers.add(new PatternMatcher(new CppIfdefPattern()));
		this.matchers.add(new PatternMatcher(new CppElsePattern()));
		this.matchers.add(new PatternMatcher(new CppElifPattern()));
		this.matchers.add(new PatternMatcher(new CppEndifPattern()));
		this.matchers.add(new PatternMatcher(new FunctionDefinitionPattern()));
		this.matchers.add(new PatternMatcher(new OpenCurlyBracketPattern()));		
		this.matchers.add(new PatternMatcher(new CloseCurlyBracketPattern()));
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
	
	private void parse() throws LexerException {
		this.nextToken();
		
		while(lookahead.getType() != TokenType.EOF) {
			
			if(lookahead.getType() == TokenType.NEWLINE)
				this.lineCounter++;
			
			else
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
		
		Stack<Ifdef> ifdef = new Stack<Ifdef>();
		Stack<Integer> bracket = new Stack<Integer>();
		ifdef.push(new Ifdef("CFILE", false));
		bracket.push(0);
		List<FunctionDefinitionOccurance> functionsToAnalyse = new ArrayList<FunctionDefinitionOccurance>();
		
		
		
		Iterator<PatternOccurance> iterator = occurances.iterator();
		
		Pattern pattern;
		PatternOccurance po;
		
		
		/*
		while(iterator.hasNext()) {
			po = iterator.next();
			pattern = po.getPattern();
			if(pattern instanceof FunctionDefinitionPattern) {
				functionsToAnalyse.forEach(f -> f.setEnd(lastBracket));
				functions.addAll(functionsToAnalyse);
				functionsToAnalyse.clear();
				functionsToAnalyse.add(new FunctionDefinitionOccurance(false, po.getStartLine()));
			} else if(pattern instanceof CloseCurlyBracketPattern) {
				lastBracket = po.getEndLine();
			}
			
		}
		*/
		
		while(iterator.hasNext()) {
			po = iterator.next();
			pattern = po.getPattern();
			
			if(pattern instanceof FunctionDefinitionPattern) {	
				if(bracket.stream().reduce((a,b) -> a | b).get() == 0) {
					functionsToAnalyse.forEach(f -> f.setEnd(lastBracket));
					functions.addAll(functionsToAnalyse);
					functionsToAnalyse.clear();
				}
				
				functionsToAnalyse.add(new FunctionDefinitionOccurance(
								po.getContent().get(1).getContent(),
								ifdef.stream().map(Ifdef::new).collect(Collectors.toList()), //deep copy of ifdef stack to list
								po.getStartLine()));
			} else if(pattern instanceof CppIfdefPattern) {
				ifdef.push(new Ifdef(po.getContent().get(2).getContent(), true));
				bracket.push(bracket.peek());
			} else if(pattern instanceof CppElsePattern) {
				ifdef.peek().setIfdef(false);
				
				bracket.push(popAndChangeTop(bracket));
			} else if(pattern instanceof CppEndifPattern) {
				ifdef.pop();
				bracket.pop();
			} else if(pattern instanceof OpenCurlyBracketPattern) {
				changeTopOfStack(bracket, true);
				
			} else if(pattern instanceof CloseCurlyBracketPattern) {
				lastBracket = po.getEndLine();
				changeTopOfStack(bracket, false);
			}
		}
		
		assert bracket.peek() == 0 : "Last function got more '{' than '}'";
		functionsToAnalyse.forEach(f -> f.setEnd(lastBracket));
		functions.addAll(functionsToAnalyse);
		functionsToAnalyse.clear();
		
		return functions;
	}
}
