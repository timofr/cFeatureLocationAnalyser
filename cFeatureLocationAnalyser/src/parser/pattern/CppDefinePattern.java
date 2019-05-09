package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public class CppDefinePattern extends CppPattern {

	private static Pattern pattern;
	
	private CppDefinePattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new CppDefinePattern() : pattern;
	}
	
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
		return this.processAbstract(occurance, lookahead, "define", true);
	}
	
}
