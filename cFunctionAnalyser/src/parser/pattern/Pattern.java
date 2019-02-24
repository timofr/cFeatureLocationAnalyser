package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.SingleTokenPatternException;
import parser.UnexpectedTokenException;

public abstract class Pattern {

	public abstract boolean match(Token lookahead);
	
	public abstract boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException, SingleTokenPatternException;
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
