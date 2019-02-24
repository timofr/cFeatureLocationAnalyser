package lexer.instructions;

import lexer.CharMatcher;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class IdentifierInstruction extends LexerInstruction {
	
	private static final CharMatcher LETTER_UNDERSCORE_MATCHER = new CharMatcher(
			new CharMatcher('a', 'z'),
			new CharMatcher('A', 'Z'),
			new CharMatcher('_'));
	
	private static final CharMatcher LETTER_UNDERSCORE_DIGIT_MATCHER = new CharMatcher(
			LETTER_UNDERSCORE_MATCHER,
			new CharMatcher('0', '9'));
	
	public IdentifierInstruction() {
		super(LETTER_UNDERSCORE_MATCHER,
				LETTER_UNDERSCORE_DIGIT_MATCHER,
				null, 
				true, false);
	}

	@Override
	public Token getToken() throws LexerException {
		return new Token(TokenType.IDENTIFIER, this.getContent());
	}
}
