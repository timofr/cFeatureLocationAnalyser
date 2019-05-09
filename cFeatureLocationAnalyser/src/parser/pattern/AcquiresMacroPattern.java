package parser.pattern;

import lexer.Token;

public class AcquiresMacroPattern extends CompilerMacroPattern {
	private static Pattern pattern;
	
	private AcquiresMacroPattern() {}
	
	public static Pattern getInstance() {
        return pattern == null ? pattern = new AcquiresMacroPattern() : pattern;
	}
	
	@Override
	public boolean match(Token lookahead) {
		return lookahead.getContent().equals("__acquires");
	}
}
