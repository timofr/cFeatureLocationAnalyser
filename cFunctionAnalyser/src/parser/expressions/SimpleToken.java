package parser.expressions;

import lexer.Token;
import lexer.Token.TokenType;

public class SimpleToken {
	private TokenType type;
	private String content;
	
	public SimpleToken(TokenType type, String content) {
		this.type = type;
		this.content = content;
	}
	
	public SimpleToken(Token token) {
		type = token.getType();
		content = token.getContent();
	}
	
	public TokenType getType() {
		return type;
	}
	public String getContent() {
		return content;
	}
	
	public void addContent(String add) {
		content = content + add;
	}
	
	@Override
	public String toString() {
		return "SimpleToken [type=" + type + ", content=" + content + "]";
	}
}
