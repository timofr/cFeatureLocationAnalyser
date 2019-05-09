package lexer;

public class CharMatcher {
	
	
	
	private Matcher matcher;
	
	public CharMatcher(char c) {
		this.matcher = new SingleCharMatcher(c);
	}
	
	public CharMatcher(char[] c) {
		this.matcher = new ArrayCharMatcher(c);
	}
	
	public CharMatcher(char startChar, char endChar) {
		this.matcher = new RangeCharMatcher(startChar, endChar);
	}
	
	public CharMatcher(CharMatcher... matchers) {
		this.matcher = new MultipleCharMatcher(matchers);
	}
	
	public boolean match(char charToMatch) {
		if(matcher.match(charToMatch))
			return true;
		return false;
	}
	
	public CharMatcher negate() {
		matcher = new NegatedCharMatcher(matcher);
		return this;
	}
	
	private Matcher getMatcher() {
		return matcher;
	}
	
	private abstract class Matcher {
		
		public abstract boolean match(char c);
		
	}
	
	private class SingleCharMatcher extends Matcher {
		private char c;
		
		public SingleCharMatcher(char c) {
			this.c = c;
		}
		
		@Override
		public boolean match(char charToMatch) {
			return this.c == charToMatch;
		}
	}
	
	private class ArrayCharMatcher extends Matcher{
		private char[] c;
		
		public ArrayCharMatcher(char[] c) {
			this.c = c;
		}
		
		@Override
		public boolean match(char charToMatch) {
			for(int i = 0; i < c.length; i++) {
				if(c[i] == charToMatch)
					return true;
			}
			return false;
		}
	}
	
	private class RangeCharMatcher extends Matcher {
		private char startChar;
		private char endChar;
		
		public RangeCharMatcher(char startChar, char endChar) {
			this.startChar = startChar;
			this.endChar = endChar;
		}
		
		@Override
		public boolean match(char charToMatch) {
			return startChar <= charToMatch && charToMatch <= endChar;
		}
	}
	
	private class NegatedCharMatcher extends Matcher {
		private final Matcher matcher;
		
		public NegatedCharMatcher(Matcher matcher) {
			this.matcher = matcher;
		}
		
		@Override
		public boolean match(char c) {
			return !this.matcher.match(c);
		}
	}
	
	private class MultipleCharMatcher extends Matcher {
		private Matcher[] matchers;
		
		public MultipleCharMatcher(CharMatcher[] charMatchers) {
			matchers = new Matcher[charMatchers.length];
			for(int i = 0; i < charMatchers.length; i++) {
				matchers[i] = charMatchers[i].getMatcher();
			}
		}
		
		@Override
		public boolean match(char c) {
			for(Matcher m : matchers) {
				if(m.match(c))
					return true;
			}
			return false;
		}
		
	}
}