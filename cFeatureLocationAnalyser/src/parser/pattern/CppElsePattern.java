package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public class CppElsePattern extends CppPattern {

	private static Pattern pattern;
	
	private CppElsePattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new CppElsePattern() : pattern;
	}
	
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
		return this.processAbstract(occurance, lookahead, "else", false);
	}
	
}
