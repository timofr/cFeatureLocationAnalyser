package lexer;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import lexer.Token.TokenType;
import lexer.instructions.CharacterInstruction;
import lexer.instructions.CommentMultilineInstruction;
import lexer.instructions.CommentSinglelineInstruction;
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

public class Lexer {
	
	private File file;
	
	public static final char EOF = (char)-1; 
	private String input;
	private int position;
	
	private boolean commentBlock = false;
	private boolean commentLine = false;
	private int line = 1;
	
	private final Set<LexerInstruction> instructions = new LinkedHashSet<>();
	private final Set<LexerInstruction> commmentInstructions = new LinkedHashSet<>();
	private Set<LexerInstruction> usedInstructionSet;
	
	public Lexer(File file, String input) {
		this.initialize();
		this.file = file;
		this.input = input;
		this.position = 0;
	}
	
	private void initialize() {
		usedInstructionSet = instructions;
		this.addInstructions(
				new CppInstruction(),
				new IdentifierInstruction(),
				new NewLineInstruction(),
				new DecimalInstruction(),
				new CommentSinglelineInstruction(),
				new CommentMultilineInstruction(),
				new OperatorInstruction(),
				new SeperatorInstruction(),
				new StringInstruction(),
				new CharacterInstruction(),
				new WhiteSpaceInstruction());
		
		this.commmentInstructions.addAll(Arrays.asList(
				new NewLineInstruction(),
				new OperatorInstruction()));
	}
	
	public void consume() {
		char la = this.lookahead(0);
		if(la == '\n')
			line++;
		this.position++;
	}
	
	
	public int getLine() {
		return line;
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
		return t;
	}
	
	public boolean hasNext() {
		return this.getLookahead() != EOF;
	}
	
//	public void match(char c) throws LexerException {
//		if(this.getLookahead() == c) {
//			this.consume();
//		} else {
//			throw new LexerException("Expected char '" + c + "' but found: " + this.getLookahead(), line);
//		}
//	}
	
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
			return new Token(TokenType.EOF, String.valueOf(Lexer.EOF), line, line);
		
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
			if(!commentBlock && !commentLine) {
				System.err.println("Lexer cannot handle char " + this.getLookahead() + " in line " + line + " in file " + file);
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
			status = instr.consumeAndUpdateLine(this.getLookahead(), line);
			
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
			case JUMP_ONE_NOT_FINISHED:
				this.consume();
				instr.append(this.getLookahead());
				this.consume();
				status = LexerStatus.GO_ON;
				break;
			case JUMP_ONE_FINISHED_CONSUMED:
				this.consume();
				instr.append(this.getLookahead());
				this.consume();
				break;
			}
		}
		
		return instr.getToken();
	}

	public File getFile() {
		return file;
	}	
}