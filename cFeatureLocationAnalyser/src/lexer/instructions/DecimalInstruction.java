package lexer.instructions;

import lexer.CharMatcher;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class DecimalInstruction extends LexerInstruction {

	private static final CharMatcher DIGITS_AND_DOT_MATCHER = new CharMatcher(
			new CharMatcher('0', '9'),
			new CharMatcher('.'));
	
	private static final CharMatcher MINUS_SIGN_MATCHER = new CharMatcher('-');
	
	public DecimalInstruction() {
		super(
				new CharMatcher(DIGITS_AND_DOT_MATCHER, MINUS_SIGN_MATCHER),
				DIGITS_AND_DOT_MATCHER,
				null, 
				true, false);
	}
	
	@Override
	protected boolean checkStartLookAhead(char c) {
		//Resolve ambiguity around the minus sign by looking ahead.
		if (!MINUS_SIGN_MATCHER.match(c)) {
			//No minus sign = no ambiguity. It must be a decimal.
			return true;
		}
		
		//If there's a minus sign, it has to be followed by
		//either a digit or a dot in order to be a decimal.
		return DIGITS_AND_DOT_MATCHER.match(this.lookAhead(1));
	}
	
	@Override
	public Token getToken() throws LexerException {
		return getNewToken(TokenType.DECIMAL);
	}

}
