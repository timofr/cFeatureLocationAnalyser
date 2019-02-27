import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import lexer.Lexer;
import parser.FunctionAnalyser;
import parser.FunctionDefinition;
import parser.Ifdef;
import parser.Parser;

public class Main {
	public static void main(String[] args) {
//		if(args.length == 0) {
//			System.out.println("Path required");
//			return;
//		}

		try(BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Timo\\Desktop\\multi.c"))) {
			StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append('\n');
		        line = br.readLine();
		    }
		    sb.append(Lexer.EOF);
		    String everything = sb.toString();
		    
		    Parser parser = new Parser(new Lexer(everything));
		    
		    parser.determineRanges();
		    List<Ifdef> ifdefs = parser.getIfdefs();
		    List<FunctionDefinition> functions = parser.getFunctions();
		    FunctionAnalyser analyser = new FunctionAnalyser(functions, ifdefs);
		    analyser.analyse();
		    analyser.printFunctionsIfdefs();
		    
		    
		    
		    //parser.printFunctionNames();
		    //ifdefs.forEach(f -> System.out.println(f.rangeToString()));
		    //functions.forEach(f -> System.out.println(f));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
