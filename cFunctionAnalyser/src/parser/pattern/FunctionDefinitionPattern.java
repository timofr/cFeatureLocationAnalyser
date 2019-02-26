package parser.pattern;

import java.util.List;

import lexer.Token;
import lexer.Token.TokenType;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public class FunctionDefinitionPattern extends Pattern {
	
	private static Pattern pattern;
	
	private FunctionDefinitionPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new FunctionDefinitionPattern() : pattern;
	}
	
	public boolean match(Token lookahead) {
		return lookahead.getType() == TokenType.IDENTIFIER;
	}
	
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
		List<Token> content = occurance.getContent();
		switch(content.size()) {
		case 1:
			if(lookahead.getType() != TokenType.IDENTIFIER)
				throw new UnexpectedTokenException();
			content.add(lookahead);
			return false;
		case 2:
			if(!lookahead.getContent().equals("("))
				throw new UnexpectedTokenException();
			content.add(lookahead);
			return false;
		default:
			if(content.contains(new Token(TokenType.SEPERATOR, ")"))) {
				
				if(lookahead.getContent().equals("{")) {
					content.add(lookahead);
					return true;
				}
				
				else if(!(lookahead.getType() == TokenType.CPP
						|| lookahead.getType() == TokenType.IDENTIFIER
						|| lookahead.getType() == TokenType.NEWLINE))
					throw new UnexpectedTokenException();
			}
			content.add(lookahead);
		}
		return false;
	}
}
