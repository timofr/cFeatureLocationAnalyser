package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public class CppElifPattern extends CppPattern{

	private static Pattern pattern;
	
	private CppElifPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new CppElifPattern() : pattern;
	}
	
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
		return this.processAbstract(occurance, lookahead, "elif", true);
	}
	
}
