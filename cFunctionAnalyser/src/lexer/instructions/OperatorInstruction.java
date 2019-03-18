package lexer.instructions;

import lexer.CharMatcher;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class OperatorInstruction extends LexerInstruction {
	public OperatorInstruction() {
		super(new CharMatcher(new char[] {'+', '-', '*', '/', '%', '<', '>', '=', '?', '.', ':', '[', ']', '@', '&', '|', '!', '~', '$', '^'}),
				null,
				null,
				true, false);
	}

	@Override
	public Token getToken() throws LexerException {
		return getNewToken(TokenType.OPERATOR);
	}
}
