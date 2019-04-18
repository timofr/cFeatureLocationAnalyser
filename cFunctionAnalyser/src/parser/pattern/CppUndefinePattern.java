package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public class CppUndefinePattern extends CppPattern {

	private static Pattern pattern;
	
	private CppUndefinePattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new CppUndefinePattern() : pattern;
	}
	
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
		return this.processAbstract(occurance, lookahead, "undefine", true);
	}
	
}
