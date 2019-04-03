package parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lexer.Token;
import lexer.Token.TokenType;
import parser.parsetree.Expression;

public class ExpressionParser {
	
	private Set<String> illegalIdentifier = new HashSet<String>();
	
	public ExpressionParser() {
		illegalIdentifier.add("defined");
		illegalIdentifier.add("FE_SUPPORTED");
		illegalIdentifier.add("IO_CONCAT");
		illegalIdentifier.add("IS_BUILTIN");
		illegalIdentifier.add("IS_ENABLED");
		illegalIdentifier.add("IS_MODULE");
		illegalIdentifier.add("IS_REACHABLE");
	}
	
	public List<String> getIdentifier(List<Token> expression) {
		return expression.stream()
			.filter(t -> t.getType() == TokenType.IDENTIFIER)
			.map(t -> t.getContent())
			.filter(s -> illegalIdentifier.contains(s))
			.collect(Collectors.toList());
	}
	
//	private List<Token> expression;
//	private Iterator<Token> iterator;
//	private Token lookahead;
//	private Map<TokenType, Supplier<Expression>> expressionHandler = new HashMap<TokenType, Supplier<Expression>>();
//	
//	
//	private void initialize() {
//		expressionHandler.put(TokenType.IDENTIFIER, this::handleIdentifier);
//		expressionHandler.put(TokenType.SEPARATOR, this::handleSeparator);
//		//expressionHandler.put(TokenType.OPERATOR, this::handleOperator);
//		iterator = expression.iterator();
//	}
//	
//	private Token lookahead() {
//		if(iterator.hasNext())
//			return lookahead = iterator.next();
//		else
//			return null;
//	}
//
//	private Expression parse() {
//		initialize();
//		return handler.get(lookahead().getType()).get();
//	}
//	
//	private Expression handleIdentifier() {
//		if(lookahead.getContent().equals("defined")) {
//			return handleDefined();
//		}
//	}
//	
//	
//
//	private Expression handleSeparator() {
//		
//		return null;
//	}
//	
////	private Expression handleOperator() {
////		return null;
////	}
//	
//	
//	
//	
//	private Expression handleDefined() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	private boolean match(String content) {
//		return lookahead.getContent().equals(content);
//	}
//	
//	private boolean match(TokenType type) {
//		return lookahead.getType() == type;
//	}
//	
//	private void check(String content) throws ExpressionParserException {
//		if(match(content))
//			throw new ExpressionParserException("Unexpected content " + lookahead.getContent() + ". Expected " + content);
//	}
//	
//	private void check(TokenType type) throws ExpressionParserException {
//		if(match(type))
//			throw new ExpressionParserException("Unexpected type " + lookahead.getType().name() + ". Expected " + type.name());
//		}
}
