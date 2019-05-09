package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public class CppIfdefPattern extends CppPattern {

	private static Pattern pattern;
	
	private CppIfdefPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new CppIfdefPattern() : pattern;
	}
	
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
		return this.processAbstract(occurance, lookahead, "ifdef", true);
	}
	

}
