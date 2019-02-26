package parser.pattern;

import lexer.Token;

public class CloseCurlyBracketPattern extends SingleTokenPattern {
	
	private static Pattern pattern;
	
	private CloseCurlyBracketPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new CloseCurlyBracketPattern() : pattern;
	}
	
	@Override
	public boolean match(Token lookahead) {
		return lookahead.getContent().equals("}");
	}

}
