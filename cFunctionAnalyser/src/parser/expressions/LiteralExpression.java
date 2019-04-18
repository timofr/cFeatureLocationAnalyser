package parser.expressions;

public class LiteralExpression extends Expression {
	private String content;

	public LiteralExpression(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "LiteralExpression [content=" + content + "]";
	}

	public String getContent() {
		return content;
	}

	
}
