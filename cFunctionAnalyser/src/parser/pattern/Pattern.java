package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.SingleTokenPattern;
import parser.UnexpectedTokenException;

public abstract class Pattern {

	public abstract boolean match(Token lookahead);
	
	public abstract boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException, SingleTokenPattern;
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
