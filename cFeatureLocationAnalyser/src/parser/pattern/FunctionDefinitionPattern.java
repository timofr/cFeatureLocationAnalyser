package parser.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lexer.Token;
import lexer.Token.TokenType;
import parser.PatternMatcher;
import parser.PatternOccurance;
import parser.UnexpectedTokenException;

public class FunctionDefinitionPattern extends Pattern {

	private static Pattern pattern;
	private static List<String> illegalNames = Arrays.asList("__releases", "__acquires");

	private FunctionDefinitionPattern() {}

	public static Pattern getInstance() {
		return pattern == null ? pattern = new FunctionDefinitionPattern() : pattern;
	}

	public boolean match(Token lookahead) {
		return lookahead.getType() == TokenType.IDENTIFIER && !illegalNames.contains(lookahead.getContent());
	}

	PatternMatcher defineMatcher = PatternMatcher.getCppDefinePatternMatcher();
	@Override
	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
		List<Token> content = occurance.getContent();
		
		PatternMatcher defineMatcher = PatternMatcher.getCppDefinePatternMatcher();
		boolean define = content.stream().anyMatch(t -> !defineMatcher.match(t).isEmpty());
		
		//performance boost
		if(lookahead.getContent().equals(";")
				|| define
				|| 10 < content.stream().filter(t -> t.getType() == TokenType.NEWLINE).count())
			//PRECARIOUS limits the function defintion pattern to 10 lines
			throw new UnexpectedTokenException(); //performance boost
		
		if (content.size() == 1) {
			if (!lookahead.getContent().equals("("))
				throw new UnexpectedTokenException();
			content.add(lookahead);
			return false;
		} else {

//			if (content.stream().anyMatch(t -> t.getContent().equals(")"))) {
//
//				if (lookahead.getContent().equals("{")) {
//					List<PatternMatcher> matchers = Arrays.asList(PatternMatcher.getAcquiresMacroPatternMatcher(),
//							PatternMatcher.getReleasesMacroPatternMatcher());
//					List<PatternOccurance> internOccurances = new ArrayList<PatternOccurance>();
//					content.add(lookahead);
//					return true;
//				}
//
//				else if (!(lookahead.getType() == TokenType.CPP || lookahead.getType() == TokenType.IDENTIFIER
//						|| lookahead.getType() == TokenType.NEWLINE))
//					throw new UnexpectedTokenException();
//			}
//			content.add(lookahead);
			
			
			
			if (lookahead.getContent().equals("{")) {
				int bracketCount = 0;
				long parametersFinished = 0;
				
				List<PatternMatcher> matchers = Arrays.asList(PatternMatcher.getAcquiresMacroPatternMatcher(),
						PatternMatcher.getReleasesMacroPatternMatcher()); //TODO add other cpp pattern
				List<PatternOccurance> internOccurances = new ArrayList<PatternOccurance>();
				
				
				
				for(Token t : content) {
					if(parametersFinished == 0) {
						if(t.getContent().equals("("))
							bracketCount++;
						else if(t.getContent().equals(")")) {
							bracketCount--;
							if(bracketCount == 0)
								parametersFinished = content.indexOf(t);
						}
					}
					else {
						for(PatternMatcher m : matchers) 
							internOccurances.addAll(m.match(t));
					}
				}
				boolean success = content.stream()
						.skip(parametersFinished + 1)
						.filter(t -> t.getType() != TokenType.NEWLINE)
						.allMatch(
								t -> internOccurances.stream()
								.anyMatch(i -> i.getContent().contains(t)));
				if(!success)
					throw new UnexpectedTokenException();
				
				
				
				
				content.add(lookahead);
				return true;
			}

			content.add(lookahead);
		}

		return false;
	}
	
	
	
//	@Override
//	public boolean process(PatternOccurance occurance, Token lookahead) throws UnexpectedTokenException {
//		List<Token> content = occurance.getContent();
//		if (content.size() == 1) {
//			if (!lookahead.getContent().equals("("))
//				throw new UnexpectedTokenException();
//			content.add(lookahead);
//			return false;
//		} else {
//			
//			long open = content.stream().filter(t -> t.getContent().equals("(")).count();
//			long close = content.stream().filter(t -> t.getContent().equals(")")).count();
//			if (open == close) {
//
//				if (lookahead.getContent().equals("{")) {
//					content.add(lookahead);
//					return true;
//				}
//
//				else if (!(lookahead.getType() == TokenType.CPP || lookahead.getType() == TokenType.IDENTIFIER
//						|| lookahead.getType() == TokenType.NEWLINE))
//					throw new UnexpectedTokenException();
//			}
//			content.add(lookahead);
//		}
//
//		return false;
//	}
}
