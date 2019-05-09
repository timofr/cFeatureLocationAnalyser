package main;

import java.util.TreeSet;
import java.util.stream.Collectors;


public class Printer {

	public static void printFunctions(PrinterData data) {
		System.out.println("Printing functions for file " + data.getFile().getAbsolutePath());
		data.getFunctions().forEach(f -> System.out.println(f));
	}
	
	public static void printIfdefs(PrinterData data) {
		System.out.println("Printing ifdefs for file " + data.getFile().getAbsolutePath());
		data.getIfdefs().forEach(f -> System.out.println(f.rangeToString()));
	}
	
	public static void printPatternOccurances(PrinterData data) {
		System.out.println("Printing all pattern occurances for file " + data.getFile().getAbsolutePath());
		data.getOccurances().forEach(o -> System.out.println(o));
	}
	
	public static void printAnalysedFunctionPositive(PrinterData data) {
		System.out.println("Printing analysed functions with positive ifdefs for file " + data.getFile().getAbsolutePath());
		data.getAnalysedFunctions().entrySet().stream()
			.forEach(e -> e.getValue().stream().filter(f -> f.getnIfdef().isEmpty()).forEach(f -> System.out.println(e.getKey() + " positive:" + f.getpIfdef())));
	}
	
	public static void printAnalysedFunctionAll(PrinterData data) {
		System.out.println("Printing analysed functions with all ifdefs for file " + data.getFile().getAbsolutePath());
		data.getAnalysedFunctions().entrySet().stream()
			.forEach(e -> e.getValue().forEach(f -> System.out.println(e.getKey() + " positive:" + f.getpIfdef() + " negative:" + f.getnIfdef())));
	}
	
	public static void printFunctionNames(PrinterData data) {
		System.out.println("Printing function names for file " + data.getFile().getAbsolutePath());
		data.getAnalysedFunctions().keySet().stream()
			.collect(Collectors.toCollection(TreeSet::new))
			.forEach(n -> System.out.println(n));
	}
}
