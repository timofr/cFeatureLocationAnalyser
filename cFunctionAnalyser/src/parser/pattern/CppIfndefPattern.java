package parser.pattern;

import lexer.Token;
import parser.PatternOccurance;
import parser.SingleTokenPatternException;
import parser.UnexpectedTokenException;

public class CppIfndefPattern extends CppPattern {

	@Override
	public boolean process(PatternOccurance occurance, Token lookahead)
			throws UnexpectedTokenException, SingleTokenPatternException {
		return processAbstract(occurance, lookahead, "ifndef", true);
	}

}
