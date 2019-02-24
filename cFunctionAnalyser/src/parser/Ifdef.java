package parser;

public class Ifdef {
	private String name;
	private boolean ifdef;
	
	public Ifdef(String name, boolean ifdef) {
		this.name = name;
		this.ifdef = ifdef;
	}
	
	public Ifdef(Ifdef i) {
		this.name = i.name;
		this.ifdef = i.ifdef;
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
	
	@Override
	public String toString() {
		return "Ifdef [name=" + name + ", ifdef=" + ifdef + "]";
	}
}
