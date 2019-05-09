package parser.pattern;

import lexer.Token;
import lexer.Token.TokenType;

public class OpenCurlyBracketPattern extends SingleTokenPattern {

	private static Pattern pattern;
	
	private OpenCurlyBracketPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new OpenCurlyBracketPattern() : pattern;
	}

	@Override
	public boolean match(Token lookahead) {
		return lookahead.getType() == TokenType.SEPARATOR && lookahead.getContent().equals("{");
	}

	
}
