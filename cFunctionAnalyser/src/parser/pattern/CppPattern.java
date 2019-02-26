package parser.pattern;

import java.util.List;

import lexer.Token;
import lexer.Token.TokenType;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public abstract class CppPattern extends Pattern {
	
	@Override
	public boolean match(Token lookahead) {
		return lookahead.getType() == TokenType.CPP;
	}
	
	public boolean processAbstract(PatternOccurance occurance, Token lookahead, String cppName, boolean identifier) throws UnexpectedTokenException {
		List<Token> content = occurance.getContent();
		switch(content.size()) {
		case 1:
			if(lookahead.getType() == TokenType.IDENTIFIER && lookahead.getContent().equals(cppName)) {
				content.add(lookahead);
				if(identifier)
					return false;
				return true;
			}
			break;
		case 2:
			if(lookahead.getType() == TokenType.IDENTIFIER) {
				content.add(lookahead);
				return true;
			}
		}
		throw new UnexpectedTokenException();
	}
}
