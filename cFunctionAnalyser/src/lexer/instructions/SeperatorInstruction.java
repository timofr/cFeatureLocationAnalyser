package lexer.instructions;

import lexer.CharMatcher;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class SeperatorInstruction extends LexerInstruction {
	public SeperatorInstruction() {
		super(new CharMatcher(new char[] {'(', ')', '{', '}', ';', ',', '\'', '\\'}),
				null,
				null,
				true, false);
	}

	@Override
	public Token getToken() throws LexerException {
		return new Token(TokenType.SEPERATOR, this.getContent());
	}
}
