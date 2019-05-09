package lexer.instructions;

import lexer.CharMatcher;
import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class StringInstruction extends LexerInstruction {
	public StringInstruction() {
		super(new CharMatcher('"'),
				new CharMatcher(new char[] {'"', Lexer.EOF}).negate(),
				new CharMatcher('"'),
				false, false);
	}

	@Override
	protected Boolean checkContentLookAhead(char c) {
		if(c == '\\') {
			return null;
		}
		return true;
	}
	
	@Override
	public Token getToken() throws LexerException {
		return getNewToken(TokenType.STRING);
	}

}
