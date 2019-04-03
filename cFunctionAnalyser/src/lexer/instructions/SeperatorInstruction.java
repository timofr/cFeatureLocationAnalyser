package lexer.instructions;

import lexer.CharMatcher;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class SeperatorInstruction extends LexerInstruction {
	public SeperatorInstruction() {
		super(new CharMatcher(new char[] {'(', ')', '{', '}', ';', ',', '\\', '`'}), 
				//PRECARIOUS '`' e.g. in linux/arch/arc/include/asm/linkage.h /* use '`' to mark new line in macro */
				//Should be solve through CharacterInstruction::checkStartLookahead
				null,
				null,
				true, false);
	}

	@Override
	public Token getToken() throws LexerException {
		return getNewToken(TokenType.SEPARATOR);
	}
}
