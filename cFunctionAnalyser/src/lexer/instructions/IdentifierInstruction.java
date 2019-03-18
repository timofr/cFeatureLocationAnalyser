package lexer.instructions;

import java.util.Arrays;
import java.util.List;

import lexer.CharMatcher;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public class IdentifierInstruction extends LexerInstruction {
	private final List<String> keywords = Arrays.asList(
			"auto", 
			"break", 
			"case", 
			"char", 
			"const", 
			"continue", 
			"default", 
			"do", 
			"double", 
			"else", 
			"enum", 
			"extern", 
			"float", 
			"for", 
			"goto", 
			"if", 
			"inline", 
			"int", 
			"long", 
			"register", 
			"restrict", 
			"return", 
			"short", 
			"signed", 
			"sizeof", 
			"static", 
			"struct", 
			"switch", 
			"typedef", 
			"union", 
			"unsigned", 
			"void", 
			"volatile", 
			"while", 
			"_Alignas", 
			"_Alignof", 
			"_Atomic", 
			"_Bool", 
			"_Complex", 
			"_Generic", 
			"_Imaginary", 
			"_Noreturn", 
			"_Static_assert", 
			"_Thread_local");
	
	

	private static final CharMatcher LETTER_UNDERSCORE_MATCHER = new CharMatcher(new CharMatcher('a', 'z'),
			new CharMatcher('A', 'Z'), new CharMatcher('_'));

	private static final CharMatcher LETTER_UNDERSCORE_DIGIT_MATCHER = new CharMatcher(LETTER_UNDERSCORE_MATCHER,
			new CharMatcher('0', '9'));

	public IdentifierInstruction() {
		super(LETTER_UNDERSCORE_MATCHER, LETTER_UNDERSCORE_DIGIT_MATCHER, null, true, false);
	}

	@Override
	public Token getToken() throws LexerException {
		return getNewToken(keywords.contains(this.getContent())? TokenType.KEYWORD : TokenType.IDENTIFIER);
	}
}
