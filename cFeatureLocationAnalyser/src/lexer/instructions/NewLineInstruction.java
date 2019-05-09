package lexer.instructions;

import lexer.CharMatcher;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class NewLineInstruction extends LexerInstruction {
	public NewLineInstruction() {
		super(new CharMatcher('\n'),
				null,
				null,
				true, false);
	}

	@Override
	public Token getToken() throws LexerException {
		return getNewToken(TokenType.NEWLINE);
	}
}
