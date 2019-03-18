package lexer.instructions;

import lexer.CharMatcher;
import lexer.IllegalCharException;
import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.Token.TokenType;

public abstract class LexerInstruction {
	
	public static enum LexerStatus {
		
		GO_ON,
		
		JUMP_ONE_NOT_FINISHED,
		
		JUMP_ONE_FINISHED_CONSUMED,
		
		FINISHED_CONSUMED,
		
		FINISHED_NOT_CONSUMED
	}
	

	private final CharMatcher startMatch, contentMatch, endMatch;
	
	private final boolean includeStart, includeEnd;

	private final StringBuilder contentBuilder = new StringBuilder();
	protected int start;
	protected int end;

	protected Lexer lookaheadProvider;


	

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
			this.start = lookaheadProvider.getLine();
			
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
	
	protected Boolean checkContentLookAhead(char c) {
		return true;
	}
	

	public LexerStatus consumeAndUpdateLine(char c, int line) throws IllegalCharException {
		LexerStatus status = this.consume(c);
		this.end = line;
		return status;
	}
	
	public LexerStatus consume(char c) throws IllegalCharException {
		//If the char matches, consume and ask for more.
		if ((this.contentMatch != null) && this.contentMatch.match(c)) {
			this.contentBuilder.append(c);
			
			Boolean contentCheck = checkContentLookAhead(c);
			
			//Content consumes next char without content match. Useful for escape characters like \n
			if(contentCheck  == null)
				return LexerStatus.JUMP_ONE_NOT_FINISHED;
			
			//Content will be finished with next char
			else if(!contentCheck) {
				return LexerStatus.JUMP_ONE_FINISHED_CONSUMED;
			}
			
			//Normal way
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
			throw new IllegalCharException(c, lookaheadProvider.getLine());
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

	public void append(char c) {
		contentBuilder.append(c);
	}
	
	protected Token getNewToken(TokenType type) {
		return new Token(type, this.getContent(), this.start, this.end);
	}
}
