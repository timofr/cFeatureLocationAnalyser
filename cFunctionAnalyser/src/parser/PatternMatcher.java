package parser;

import java.util.LinkedList;
import java.util.List;

import lexer.Token;
import parser.pattern.Pattern;

public class PatternMatcher {
	
	Pattern pattern;
	List<PatternOccurance> currentPatterns = new LinkedList<PatternOccurance>();
	
	public PatternMatcher(Pattern pattern) {
		this.pattern = pattern;
	}
	
	public List<PatternOccurance> match(Token lookahead, int line) {
		List<PatternOccurance> occurance = new LinkedList<PatternOccurance>();
		List<PatternOccurance> oToRemove = new LinkedList<PatternOccurance>();
		for(PatternOccurance o : currentPatterns) {
			try {
				if(pattern.process(o, lookahead)) {
					oToRemove.add(o);
					occurance.add(o);
					o.setEndLine(line);
				}
			} catch (UnexpectedTokenException e) {
				oToRemove.add(o);
			} catch (SingleTokenPattern e) {
				oToRemove.add(o);
				occurance.add(o);
				o.setEndLine(o.getStartLine());
			}
		}
		
		currentPatterns.removeAll(oToRemove);
		
		if(this.pattern.match(lookahead))
			currentPatterns.add(new PatternOccurance(this.pattern, lookahead, line));
		return occurance;
	}
}
