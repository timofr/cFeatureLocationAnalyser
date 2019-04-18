package parser.expressions;

public class FunctionExpression extends UnaryExpression {

	public FunctionExpression(String operator, String macro) {
		super(operator, new IdentifierExpression(macro));
	}

}
