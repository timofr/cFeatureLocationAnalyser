package lexer.instructions;

import lexer.CharMatcher;
import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class CommentSinglelineInstruction extends LexerInstruction {
	private static final CharMatcher SLASH_MATCHER = new CharMatcher('/');
	
	public CommentSinglelineInstruction() {
		super(new CharMatcher('/'),
				new CharMatcher(new char[] {'\n', Lexer.EOF}).negate(),
				new CharMatcher('\n'),
				true, false);
	}
	
	@Override
	protected boolean checkStartLookAhead(char c) {
		return SLASH_MATCHER.match(this.lookAhead(1));
	}

	@Override
	public Token getToken() throws LexerException {
		return getNewToken(TokenType.COMMENT);
	}

}
