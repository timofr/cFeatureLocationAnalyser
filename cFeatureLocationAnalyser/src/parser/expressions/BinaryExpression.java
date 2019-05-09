package parser.expressions;

public class BinaryExpression extends Expression {

	final Expression left;
	final String operator;
	final Expression right;

	public BinaryExpression(Expression left, String operator, Expression right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	@Override
	public String toString() {
		return "BinaryExpression [left=" + left + ", operator=" + operator + ", right=" + right + "]";
	}
	
}
