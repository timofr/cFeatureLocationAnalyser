package lexer.instructions;

import lexer.CharMatcher;
import lexer.IllegalCharException;
import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;

public abstract class LexerInstruction {
	
	public static enum LexerStatus {
		
		GO_ON,
		
		FINISHED_CONSUMED,
		
		FINISHED_NOT_CONSUMED
	}
	

	private final CharMatcher startMatch, contentMatch, endMatch;
	
	private final boolean includeStart, includeEnd;

	private final StringBuilder contentBuilder = new StringBuilder();
	

	private Lexer lookaheadProvider;
	

	public LexerInstruction(CharMatcher startMatch, CharMatcher contentMatch,
			CharMatcher endMatch,  boolean includeStart, boolean includeEnd) {
		this.startMatch = startMatch;
		this.contentMatch = contentMatch;
		this.endMatch = endMatch;
		
		this.includeStart = includeStart;
		this.includeEnd = includeEnd;
	}

	public void provideLookAhead(Lexer provider) {
		assert this.lookaheadProvider == null : "Lookahead provider set twice!";
		this.lookaheadProvider = provider;
	}

	protected char lookAhead(int distance) {
		assert this.lookaheadProvider != null : "No lookahead provider set for an instruction that needs lookahead!";
		return this.lookaheadProvider.lookahead(distance);
	}
	

	public boolean isStart(char c) {
		boolean matches = this.startMatch.match(c) && this.checkStartLookAhead(c);
		
		if (matches) {
			//Clear StringBuilder for next token
			this.contentBuilder.setLength(0);
			
			//Include start.
			if (this.includeStart) {
				this.contentBuilder.append(c);
			}
		}
		
		return matches;
	}
	
	
	protected boolean checkStartLookAhead(char c) {
		return true;
	}
	

	public LexerStatus consume(char c) throws IllegalCharException {
		//If the char matches, consume and ask for more.
		if ((this.contentMatch != null) && this.contentMatch.match(c)) {
			this.contentBuilder.append(c);
			return LexerStatus.GO_ON;
		}
		
		//If this LexerInstruction should include the ending char,
		//append it to the content builder
		if (this.includeEnd) {
			this.contentBuilder.append(c);
		}
		
		//If this LexerInstruction has an end,
		//check for an illegal ending char
		if ((this.endMatch != null) && !this.endMatch.match(c)) {
			System.err.println("Current content " + contentBuilder.toString());
			throw new IllegalCharException(c);
		}
		
		if (this.endMatch == null) {
			//No ending char present
			return LexerStatus.FINISHED_NOT_CONSUMED;
		} else {
			//Ending char present
			//Consume it
			return LexerStatus.FINISHED_CONSUMED;
		}
	}
	
	protected String getContent() {
		return this.contentBuilder.toString();
	}

	public abstract Token getToken() throws LexerException;
}
