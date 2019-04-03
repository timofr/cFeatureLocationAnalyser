package parser.pattern;

import java.util.List;

import lexer.Token;
import parser.PatternOccurance;
import parser.SingleTokenPatternException;
import parser.UnexpectedTokenException;

public abstract class CompilerMacroPattern extends Pattern {

	@Override
	public boolean process(PatternOccurance occurance, Token lookahead)
			throws UnexpectedTokenException, SingleTokenPatternException {
		List<Token> content = occurance.getContent();
		if(content.size() == 1) {
			if (!lookahead.getContent().equals("("))
				throw new UnexpectedTokenException();
			content.add(lookahead);
			return false;
		}
		else {
			content.add(lookahead);
			long open = content.stream().filter(t -> t.getContent().equals("(")).count();
			long close = content.stream().filter(t -> t.getContent().equals(")")).count();
			return open == close;
		}
	}

}
