package parser.expressions;

public class UnaryExpression extends Expression {
	final String operator;
	final Expression right;

	public UnaryExpression(String operator, Expression right) {
		this.operator = operator;
		this.right = right;
	}
	
	@Override
	public String toString() {
		return "UnaryExpression [operator=" + operator + ", right=" + right + "]";
	}
}
