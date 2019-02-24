import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import lexer.Lexer;
import parser.FunctionDefinitionOccurance;
import parser.Parser;

public class Main {
	public static void main(String[] args) {
		if(args.length == 0) {
			System.out.println("Path required");
			return;
		}
		
		try(BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
			StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append('\n');
		        line = br.readLine();
		    }
		    sb.append(Lexer.EOF);
		    String everything = sb.toString();
		    
		    Lexer lexer = new Lexer(everything);
		    Parser parser = new Parser(lexer);
		    
		    List<FunctionDefinitionOccurance> functions = parser.searchForFunctionRanges();
		    functions.forEach(f -> System.out.println(f));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
