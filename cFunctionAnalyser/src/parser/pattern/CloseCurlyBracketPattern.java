package parser.pattern;

import lexer.Token;

public class CloseCurlyBracketPattern extends SingleTokenPattern {

	@Override
	public boolean match(Token lookahead) {
		return lookahead.getContent().equals("}");
	}

}
