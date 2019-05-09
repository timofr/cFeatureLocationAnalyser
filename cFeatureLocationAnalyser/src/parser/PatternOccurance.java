package parser;

import java.util.LinkedList;
import java.util.List;

import lexer.Token;
import parser.pattern.Pattern;

public class PatternOccurance {
	private int startLine;

	private int endLine;
	
	private List<Token> content = new LinkedList<Token>();
	private Pattern pattern;
	
	public PatternOccurance(Pattern pattern, Token token) {
		this.pattern = pattern;
		this.content.add(token);
		this.startLine = token.getStart();
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public List<Token> getContent() {
		return content;
	}

	public void setContent(List<Token> content) {
		this.content = content;
	}

	public int getStartLine() {
		return startLine;
	}

	public Pattern getPattern() {
		return pattern;
	}
	
	@Override
	public String toString() {
		return "PatternOccurance [pattern=" + pattern + ", startLine=" + startLine + ", endLine=" + endLine + ", content=" + content
				+ "]";
	}

}
