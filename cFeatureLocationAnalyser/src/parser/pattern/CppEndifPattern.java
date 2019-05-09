package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public class CppEndifPattern extends CppPattern {

	private static Pattern pattern;
	
	private CppEndifPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new CppEndifPattern() : pattern;
	}
	
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
		return this.processAbstract(occurance, lookahead, "endif", false);
	}
	
}
