package lexer.instructions;

import lexer.CharMatcher;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class CppInstruction extends LexerInstruction {

	public CppInstruction() {
		super(new CharMatcher('#'),
				null,
				null,
				true, false);
	}
	
	@Override
	public Token getToken() throws LexerException {
		return getNewToken(TokenType.CPP);
	}

}
