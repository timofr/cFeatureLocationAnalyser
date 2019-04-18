package parser.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class ExpressionParser {
	private final List<SimpleToken> tokens;
	private final Iterator<SimpleToken> iterator;
	private SimpleToken lookahead;
	private static final Set<String> functions = new HashSet<String>();
	
	static {
		//functions.add("FE_SUPPORTED");
		//functions.add("IO_CONCAT");
		functions.add("IS_BUILTIN");
		functions.add("IS_ENABLED");
		functions.add("IS_MODULE");
		functions.add("IS_REACHABLE");
	}
	
	
	public ExpressionParser(List<Token> tokens) {
		this.tokens = parseSimpleTokens(tokens);
		this.iterator = this.tokens.iterator();
	}
	
	public static List<String> getIdentifierSimple(List<SimpleToken> expression) {
		return expression.stream()
			.filter(t -> t.getType() == TokenType.IDENTIFIER)
			.map(t -> t.getContent())
			.filter(s -> !(functions.contains(s) || s.equals("defined") || s.equals("FE_SUPPORTED") || s.equals("IO_CONCAT")))
			.collect(Collectors.toList());
	}
	
	
	public static List<String> getIdentifier(String expression) {
		Lexer lexer = new Lexer(null, expression);//Null wont cause any problems because the string was already lexed.
		List<Token> ts = null;
		try {
			ts = lexer.getTokens();
		}
		catch (LexerException e) { //This should never happen
			System.err.println("Unexpected exception in expression parser");
			e.printStackTrace();
		}
		
		
		List<SimpleToken> simpleTokens = parseSimpleTokens(ts);
		return getIdentifierSimple(simpleTokens);
	}
	
	public static List<String> getIdentifier(List<Token> expression) {
		List<SimpleToken> simpleTokens = parseSimpleTokens(expression);
		return getIdentifierSimple(simpleTokens);
	}
	
	public static List<String> positiveIdentifiers(String ifdef) {
		if(ifdef.split("\\s+").length < 2)
			return Arrays.asList(ifdef);
		
		List<String> identifiers = new ArrayList<String>();
		Lexer lexer = new Lexer(null, ifdef);//Null wont cause any problems because the string was already lexed.
		List<Token> ts = null;
		try {
			ts = lexer.getTokens();
		}
		catch (LexerException e) { //This should never happen
			System.err.println("Unexpected exception in expression parser");
			e.printStackTrace();
		}
		
		ExpressionParser parser = new ExpressionParser(ts);
				
		try {
			Expression expr = parser.parse();
			parser.traverseExpressionTree(expr, identifiers, 0);
		}
		catch (ExpressionParserException e) {
			System.err.println("Failed to parse expression:");
			System.err.println(ifdef);
			e.printStackTrace();
			return getIdentifierSimple(parser.tokens);
		}
		
		
		return identifiers;
	}
	
	private void traverseExpressionTree(Expression expr, List<String> identifiers, int neg) throws ExpressionParserException {
		if(expr instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) expr;
			traverseExpressionTree(be.left, identifiers, neg);
			traverseExpressionTree(be.right, identifiers, neg);
		}
		else if(expr instanceof UnaryExpression) {
			UnaryExpression ue = (UnaryExpression) expr;
			if(ue.operator.equals("!")) {
				neg++;
				traverseExpressionTree(ue.right, identifiers, neg);
			}
			else if(ue.operator.equals("defined")) {
				if(neg % 2 == 0) {
					if(!(ue.right instanceof IdentifierExpression))
						throw new ExpressionParserException("No identifier followed by defined.");
					identifiers.add(((IdentifierExpression) ue.right).getContent());
				}
			}	
		}
	}
	
	public Expression parse() throws ExpressionParserException {
		consume();
		return expression();
	}
	
	private Expression expression() throws ExpressionParserException {
	    return logicalOr();       
	}

	private Expression logicalOr() throws ExpressionParserException {
		Expression expr = logicalAnd();
		while (match("||")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = logicalAnd();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}
	

	private Expression logicalAnd() throws ExpressionParserException {
		Expression expr = bitwiseOr();
		while (match("&&")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = bitwiseOr();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}
	
	private Expression bitwiseOr() throws ExpressionParserException {
		Expression expr = bitwiseXor();
		while (match("|")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = bitwiseXor();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}
	
	private Expression bitwiseXor() throws ExpressionParserException {
		Expression expr = bitwiseAnd();
		while (match("^")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = bitwiseAnd();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}
	
	private Expression bitwiseAnd() throws ExpressionParserException {
		Expression expr = equality();
		while (match("&")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = equality();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}
	
	private Expression equality() throws ExpressionParserException {
		Expression expr = comparison();
		while (match("!=") || match("==")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = comparison();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}

	private Expression comparison() throws ExpressionParserException {
		Expression expr = shift();
		while (match("<") || match("<=") || match(">") || match(">=")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = shift();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}

	private Expression shift() throws ExpressionParserException {
		Expression expr = addition();
		while (match("<<") || match(">>")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = addition();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}
	
	private Expression addition() throws ExpressionParserException {
		Expression expr = multiplication();
		while (match("+") || match("-")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = multiplication();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}

	private Expression multiplication() throws ExpressionParserException {
		Expression expr = unary();
		while (match("*") || match("/") || match("%")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = unary();
			expr = new BinaryExpression(expr, operator, right);
		}

		return expr;
	}

	private Expression unary() throws ExpressionParserException {
		if (match("!")) {
			String operator = lookahead.getContent();
			consume();
			Expression right = unary();
			return new UnaryExpression(operator, right);
		}
		
		if(match("defined")) {
			consume();
			Expression right = unary();
			return new DefinedExpression(right);
		}
		
		if (functions.contains(lookahead.getContent())) {
			return function();
		}
		
		if(match("(")) {
			consume();
			return brackets();
		}
		return primary();
	}

	private Expression function() throws ExpressionParserException {
		String functionName = lookahead.getContent();
		consume();
		check("(");
		consume();
		String macro = lookahead.getContent();
		consume();
		check(")");
		return new FunctionExpression(functionName, macro);
	}
	
	private Expression primary() throws ExpressionParserException {
		if (match(TokenType.DECIMAL)) {
			String c = lookahead.getContent();
			consume();
			return new DecimalExpression(c);
		}
		
		if (match(TokenType.IDENTIFIER)) {
			String c = lookahead.getContent();
			consume();
			return new IdentifierExpression(c);
		}
		throw new ExpressionParserException("Unexpected token " + lookahead.getContent() + ". Expected identifier or decimal.");
	}
	
	private Expression brackets() throws ExpressionParserException {
		Expression expr = expression();
		check(")");
		consume();
		return expr;
	}
	
	
	
	private SimpleToken consume() {
		if (iterator.hasNext())
			return lookahead = iterator.next();
		return null;
	}

	private boolean match(String content) {
		return lookahead.getContent().equals(content);
	}
	
   
	private boolean match(TokenType type) {
		return lookahead.getType() == type;
	}
	
	private void check(String content) throws ExpressionParserException {
		if(!match(content))
			throw new ExpressionParserException("Unexpected content " + lookahead.getContent() + ". Expected " + content);
	}
	
	
	private static List<SimpleToken> parseSimpleTokens(List<Token> tokens) {
		List<SimpleToken> simpleTokens = new ArrayList<SimpleToken>();
		for(Token t : tokens) {
			if(simpleTokens.size() != 0) {
				SimpleToken last = simpleTokens.get(simpleTokens.size() - 1);
				if(last.getType() == TokenType.OPERATOR && t.getType() == TokenType.OPERATOR && !t.getContent().equals("!"))
					last.addContent(t.getContent());
				else
					simpleTokens.add(new SimpleToken(t));
			}
			else
				simpleTokens.add(new SimpleToken(t));
		}
		return simpleTokens;
	}
	
}

