package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.SingleTokenPatternException;

public abstract class SingleTokenPattern extends Pattern {
	
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws SingleTokenPatternException {
		throw new SingleTokenPatternException();
	}
	
}
