package lexer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import lexer.Token.TokenType;
import lexer.instructions.CppInstruction;
import lexer.instructions.DecimalInstruction;
import lexer.instructions.IdentifierInstruction;
import lexer.instructions.LexerInstruction;
import lexer.instructions.NewLineInstruction;
import lexer.instructions.OperatorInstruction;
import lexer.instructions.SeperatorInstruction;
import lexer.instructions.StringInstruction;
import lexer.instructions.WhiteSpaceInstruction;
import lexer.instructions.LexerInstruction.LexerStatus;
import main.Main;

public class Lexer {
	
	public static final char EOF = (char)-1; 
	private String input;
	private int position;
	
	private boolean comment = false;
	private int line = 1;
	
	private final Set<LexerInstruction> instructions = new LinkedHashSet<>();
	private final Set<LexerInstruction> commmentInstructions = new LinkedHashSet<>();
	private Set<LexerInstruction> usedInstructionSet;
	
	public Lexer(String input) {
		this.initialize();
		this.input = input;
		this.position = -1;
		this.consume();
	}
	
	private void initialize() {
		usedInstructionSet = instructions;
		this.addInstructions(
				new CppInstruction(),
				new IdentifierInstruction(),
				new NewLineInstruction(),
				new OperatorInstruction(),
				new SeperatorInstruction(),
				new DecimalInstruction(),
				new StringInstruction(),
				new WhiteSpaceInstruction());
		
		this.commmentInstructions.addAll(Arrays.asList(
				new NewLineInstruction(),
				new OperatorInstruction()));
	}
	
	public void consume() {
		this.position++;
	}
	
	
	private void addInstruction(LexerInstruction instr) {
		this.instructions.add(instr);
		
		//Some instructions need to access some lookahead char
		instr.provideLookAhead(this);
	}
	
	private void addInstructions(LexerInstruction... instrs) {
		for(LexerInstruction i : instrs)
			this.addInstruction(i);
	}
	
	public Token nextToken() throws LexerException {
		Token t = null;
		while (t == null) {
			t = this.produceNextToken();
		}
		if(t.getType() == TokenType.NEWLINE)
			line++;
		if(!this.comment && t.getContent().contains("/*") && !t.getContent().contains("*/")) {
			comment = true;
			usedInstructionSet = commmentInstructions;
		}
		else if(this.comment && t.getContent().contains("*/")) {
			comment = false;
			usedInstructionSet = instructions;
		}
		return t;
	}
	
	public boolean hasNext() {
		return this.getLookahead() != EOF;
	}
	
	public void match(char c) throws LexerException {
		if(this.getLookahead() == c) {
			this.consume();
		} else {
			throw new LexerException("Expected char '" + c + "' but found: " + this.getLookahead());
		}
	}
	
	public char lookahead(int characters) {
		int lookaheadPosition = this.position + characters;
		if (lookaheadPosition < this.input.length()) {
			return this.input.charAt(lookaheadPosition);
		}
		return EOF;
	}
	
	private char getLookahead() {
		return this.lookahead(0);
	}
	
	private Token produceNextToken() throws LexerException {
		if (!this.hasNext())
			return new Token(TokenType.EOF, String.valueOf(Lexer.EOF));
		
		//Find instruction which can handle the lookahead char
		LexerInstruction instr = null;
		for (LexerInstruction i: this.usedInstructionSet) {
			if (i.isStart(this.getLookahead())) {
				instr = i;
				break;
			}
		}

		//If no instruction can handle this char it is illegal
		if (instr == null) {
			if(!comment) {
				System.err.println("Lexer cannot handle char " + this.getLookahead() + " in line " + line + " in file " + Main.path);
			}
			this.consume();
			return null;
		}
		//	throw new IllegalCharException(this.getLookahead());
			
		//Consume the first char of the token.
		this.consume();
		
		//Instruction consumes chars until it says that it is finished
		LexerStatus status = LexerStatus.GO_ON;
		while (status == LexerStatus.GO_ON) {
			//No need of checking for EOF
			//The instruction is handle to handle it
			//It will take the appropriate action (ending or throwing)
			status = instr.consume(this.getLookahead());
			
			//Consume next char, if needed
			switch (status) {
			case GO_ON:
				this.consume();
				break;
			case FINISHED_CONSUMED:
				this.consume();
				break;
			case FINISHED_NOT_CONSUMED:
				break;
			}
		}
		
		return instr.getToken();
	}	
}