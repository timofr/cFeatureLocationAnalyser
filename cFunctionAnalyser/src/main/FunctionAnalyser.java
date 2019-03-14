package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import lexer.Lexer;
import parser.Parser;

public class FunctionAnalyser {

	
	public static PrinterData anaylse(File file) {
	    
	    try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append('\n');
		        line = br.readLine();
		    }
		    //sb.append(Lexer.EOF);
		    
		    Parser parser = new Parser(new Lexer(file, sb.toString()));
		    PrinterData printerData = new PrinterData(parser);
		    
		    parser.analyse();
		    
		    return printerData;
		}
		catch(Exception e) {
			System.err.println("Failed in file " + file.getAbsolutePath());
			e.printStackTrace();
		}
		return null;
	}
}
