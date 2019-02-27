package parser;

public class Ifdef {
	private String name;
	private boolean ifdef;
	private int startLine;
	private int elseLine;
	private int endLine;
	
	
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

	public Ifdef(String name, int startLine) {
		this.name = name;
		this.ifdef = true;
		this.startLine = startLine;
		this.elseLine = -1;
		this.endLine = -1;
	}
	
	public Ifdef(Ifdef i) {
		this.name = i.name;
		this.ifdef = i.ifdef;
		this.startLine = i.startLine;
		this.elseLine = i.elseLine;
		this.endLine = i.endLine;
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
	
	public String rangeToString() {
		return name + " " + startLine + " "  + elseLine + " " + endLine; 
	}
	
	@Override
	public String toString() {
		return "Ifdef [name=" + name + ", ifdef=" + ifdef + "]";
	}
}
