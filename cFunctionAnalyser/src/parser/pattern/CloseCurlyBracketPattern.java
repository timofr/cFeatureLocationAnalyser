package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.SingleTokenPattern;

public class CloseCurlyBracketPattern extends Pattern {

	@Override
	public boolean match(Token lookahead) {
		return lookahead.getContent().equals("}");
	}

	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws SingleTokenPattern {
		throw new SingleTokenPattern();
	}

}
