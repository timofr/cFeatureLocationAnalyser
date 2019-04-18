package parser;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Ifdef implements Serializable{
	
	private static final long serialVersionUID = -8167505029807088870L;
	
	private String name;
	private boolean ifdef;
	private boolean n; //is it ifndef block
	private int startLine;
	private int elseLine;
	private int endLine;
	private Set<String> define = new HashSet<String>();
	private Set<String> undefine = new HashSet<String>();
	
	public Ifdef(String name) {
		this.name = name;
		this.ifdef = true;
		this.n = false;
		this.startLine = -1;
		this.elseLine = -1;
		this.endLine = -1;
	}
	
	public Ifdef(String name, boolean ifdef, int startLine) {
		this.name = name;
		this.ifdef = ifdef;
		this.n = !ifdef;
		this.startLine = startLine;
		this.elseLine = -1;
		this.endLine = -1;
	}
	
	public Ifdef(Ifdef i) {
		this.name = i.name;
		this.ifdef = i.ifdef;
		this.n = i.n;
		this.startLine = i.startLine;
		this.elseLine = i.elseLine;
		this.endLine = i.endLine;
	}
	
	public boolean isN() {
		return n;
	}

	public void setN(boolean n) {
		this.n = n;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getElseLine() {
		return elseLine;
	}

	public void setElseLine(int elseLine) {
		this.elseLine = elseLine;
	}

	
	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isIfdef() {
		return ifdef;
	}
	public void setIfdef(boolean ifdef) {
		this.ifdef = ifdef;
	}
	
	public void addDefine(String s) {
		define.add(s);
	}
	
	public void addUndefine(String s) {
		undefine.add(s);
	}
	
	public String rangeToString() {
		return "Ifdef [name=" + name + ", n=" + n + ", startLine=" + startLine + ", elseLine=" + elseLine + ", endLine=" + endLine
				+ "]"; 
	}
	
	@Override
	public String toString() {
		return "Ifdef [name=" + name + ", ifdef=" + ifdef +"]";
	}
}
