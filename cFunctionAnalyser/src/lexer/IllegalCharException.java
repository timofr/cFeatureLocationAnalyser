package lexer;

public class IllegalCharException extends LexerException {
	
	private static final long serialVersionUID = 1L;

	public IllegalCharException(char c, int line) {
		super("Illegal char in input string: " + c, line);
	}

}
