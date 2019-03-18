package lexer.instructions;

import lexer.CharMatcher;
import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class CharacterInstruction extends LexerInstruction {
	public CharacterInstruction() {
		super(new CharMatcher('\''),
				new CharMatcher(new char[] {'\'', Lexer.EOF}).negate(),
				new CharMatcher('\''),
				false, false);
	}

	@Override
	protected boolean checkStartLookAhead(char c) {
		if(lookAhead(1) == '\\') 
			return lookAhead(3) == '\'';
		else 
			return lookAhead(2) == '\'';
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
		return getNewToken(TokenType.CHARACTER);
	}
}
