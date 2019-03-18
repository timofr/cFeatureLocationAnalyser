package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import lexer.Lexer;
import parser.Parser;

public class FunctionAnalyser {

	
	public static PrinterData anaylse(File file) {
		StringBuilder sb = new StringBuilder();
	    try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append('\n');
		        line = br.readLine();
		    }
		    //sb.append(Lexer.EOF);
		}
		catch(IOException e) {
			System.err.println("Failed to read file " + file.getAbsolutePath());
			e.printStackTrace();
		}
	    
	    Parser parser = new Parser(new Lexer(file, sb.toString()));
	    PrinterData printerData = new PrinterData(parser);
	    parser.analyse();
	    
	    return printerData;
	}
	
}
