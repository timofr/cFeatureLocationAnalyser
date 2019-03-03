package main;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import parser.FunctionDefinition;
import parser.Ifdef;
import parser.Parser;
import parser.PatternOccurance;

public class Printer {

	private List<FunctionDefinition> functions;
	private List<Ifdef> ifdefs;
	private Map<FunctionDefinition, Map<Ifdef, Boolean>> analysedFunctions;
	private List<PatternOccurance> occurances;
	
	public Printer(Parser parser) {
		this.functions = parser.getFunctions();
		this.ifdefs = parser.getIfdefs();
		this.analysedFunctions = parser.getAnalysedFunctions();
		this.occurances = parser.getOccurances();
	}
	
	public void printFunctions() {
		System.out.println("Printing functions...");
		functions.forEach(f -> System.out.println(f));
	}
	
	public void printIfdefs() {
		System.out.println("Printing ifdefs...");
		ifdefs.forEach(f -> System.out.println(f.rangeToString()));
	}
	
	public void printPatternOccurances() {
		System.out.println("Printing all pattern occurances...");
		occurances.forEach(o -> System.out.println(o));
	}
	
	public void printAnalysedFunctionPositive() {
		System.out.println("Printing analysed functions with positive ifdefs...");
		analysedFunctions.entrySet().stream()
			.forEach(e -> System.out.println(e.getKey().getName() + ifdefMapToString(e.getValue())));
	}
	
	public void printAnalysedFunctionAll() {
		System.out.println("Printing analysed functions with all ifdefs...");
		analysedFunctions.entrySet().stream()
			.forEach(e -> System.out.println(e.getKey().getName() + ifdefMapToString(e.getValue())));
	}
	
	public void printFunctionNames() {
		System.out.println("Printing function names...");
		functions.stream()
			.map(f -> f.getName())
			.collect(Collectors.toCollection(TreeSet::new))
			.forEach(n -> System.out.println(n));
	}
	
	private String ifdefMapToString(Map<Ifdef, Boolean> map) {
		if(map.size() == 0)
			return "";
		
		StringBuilder builder = new StringBuilder(" ");
		map.entrySet().stream().filter(e -> e.getValue()).forEach(e -> builder.append("p:").append(e.getKey().getName()).append(" "));
		map.entrySet().stream().filter(e -> !e.getValue()).forEach(e -> builder.append("n:").append(e.getKey().getName()).append(" "));
		
		return builder.substring(0, builder.length() - 1);
	}
	
}
