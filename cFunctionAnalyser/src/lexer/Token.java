package lexer;

public class Token {
	
	public static enum TokenType {
		EOF,
		COMMENT,
		IDENTIFIER,
		DECIMAL,
		OPERATOR,
		SEPERATOR,
		NEWLINE,
		STRING
	}
	
	private final TokenType type;
	private final String content;
	
	public Token(TokenType type, String content) {
		this.type = type;
		this.content = content;
	}

	@Override
	public String toString() {
		return "Token [type=" + type + ", content=" + content + "]";
	}

	public TokenType getType() {
		return this.type;
	}
	
	public String getContent() {
		return this.content;
	}
	
}
