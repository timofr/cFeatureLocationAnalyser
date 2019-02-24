package lexer;

public class IllegalCharException extends LexerException {
	
	private static final long serialVersionUID = 1L;

	public IllegalCharException(char c) {
		super("Illegal char in input string: " + c);
	}

}
