package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.SingleTokenPattern;
import parser.UnexpectedTokenException;

public class OpenCurlyBracketPattern extends Pattern {


	@Override
	public boolean match(Token lookahead) {
		return lookahead.getContent().equals("{");
	}

	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException, SingleTokenPattern {
		throw new SingleTokenPattern();
	}

	
}
