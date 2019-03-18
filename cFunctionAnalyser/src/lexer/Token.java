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
		STRING,
		CPP,
		KEYWORD,
		CHARACTER
	}
	
	private final TokenType type;
	private final String content;
	private final int start;
	private final int end;
	
	public Token(TokenType type, String content, int start, int end) {
		this.type = type;
		this.content = content;
		this.start = start;
		this.end = end;
	}

	public TokenType getType() {
		return this.type;
	}
	
	public String getContent() {
		return this.content;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "Token [type=" + type + ", content=" + content + ", start=" + start + ", end=" + end + "]";
	}
}
