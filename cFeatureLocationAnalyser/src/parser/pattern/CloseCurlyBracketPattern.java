package parser.pattern;

import lexer.Token;
import lexer.Token.TokenType;

public class CloseCurlyBracketPattern extends SingleTokenPattern {
	
	private static Pattern pattern;
	
	private CloseCurlyBracketPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new CloseCurlyBracketPattern() : pattern;
	}
	
	@Override
	public boolean match(Token lookahead) {
		return lookahead.getType() == TokenType.SEPARATOR && lookahead.getContent().equals("}");
	}

}
