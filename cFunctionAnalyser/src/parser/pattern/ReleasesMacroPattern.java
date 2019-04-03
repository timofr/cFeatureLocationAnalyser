package parser.pattern;

import lexer.Token;

public class ReleasesMacroPattern extends CompilerMacroPattern {
	private static Pattern pattern;
	
	private ReleasesMacroPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new ReleasesMacroPattern() : pattern;
	}
	
	@Override
	public boolean match(Token lookahead) {
		return lookahead.getContent().equals("__releases");
	}
}
