package lexer.instructions;

import lexer.CharMatcher;
import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class CommentMultilineInstruction extends LexerInstruction {
	private static final CharMatcher ASTERISK_MATCHER = new CharMatcher('*');
	
	public CommentMultilineInstruction() {
		super(new CharMatcher('/'),
				new CharMatcher(Lexer.EOF).negate(),
				null,
				true, true);
	}
	
	@Override
	protected boolean checkStartLookAhead(char c) {
		return ASTERISK_MATCHER.match(this.lookAhead(1));
	}
	
	@Override
	protected Boolean checkContentLookAhead(char c) {
		if(c == '*' && this.lookAhead(1) == '/') {
			return false;
		}
		return true;
	}
	
	

	@Override
	public Token getToken() throws LexerException {
		return getNewToken(TokenType.COMMENT);
	}
}
