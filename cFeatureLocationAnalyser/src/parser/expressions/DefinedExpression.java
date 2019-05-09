package parser.expressions;

public class DefinedExpression extends UnaryExpression {
	public DefinedExpression(Expression right) {
		super("defined", right);
	}
}
