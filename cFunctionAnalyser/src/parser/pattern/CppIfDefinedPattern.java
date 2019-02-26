package parser.pattern;

import java.util.List;

import lexer.Token;
import lexer.Token.TokenType;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public class CppIfDefinedPattern extends CppPattern {

	private static Pattern pattern;
	
	private CppIfDefinedPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new CppIfDefinedPattern() : pattern;
	}
	
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
		List<Token> content = occurance.getContent();
		switch(content.size()) {
		case 1:
			if(lookahead.getType() == TokenType.IDENTIFIER && lookahead.getContent().equals("if")) {
				content.add(lookahead);
				return false;
			}
			break;
		case 2:
			if(lookahead.getType() == TokenType.IDENTIFIER && lookahead.getContent().equals("defined")) {
				content.add(lookahead);
				return false;
			} else if(lookahead.getType() == TokenType.SEPERATOR && lookahead.getContent().equals("(")) {
				content.add(lookahead);
				return false;
			}
			break;
//		case 3:
//			if(lookahead.getType() == TokenType.IDENTIFIER) {
//				content.add(lookahead);
//				return true;
//			}
		default:
			if(lookahead.getType() == TokenType.NEWLINE)
				return true;
			content.add(lookahead);
			return false;
		}
		throw new UnexpectedTokenException();
	}
	
}
