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
		KEYWORD
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Token))
			return false;
		Token other = (Token) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
