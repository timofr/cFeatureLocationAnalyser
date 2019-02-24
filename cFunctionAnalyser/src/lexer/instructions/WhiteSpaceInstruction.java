package lexer.instructions;

import lexer.CharMatcher;
import lexer.LexerException;
import lexer.Token;

public class WhiteSpaceInstruction extends LexerInstruction {
	public WhiteSpaceInstruction() {
		super(new CharMatcher(new char[] {' ', '\t'}),
			null,
			null, true, false);
	}

	@Override
	public Token getToken() throws LexerException {
		return null;
	}
}
