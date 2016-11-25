package lexicalanalysis;

import java.util.function.Predicate;

import com.google.common.base.CharMatcher;

public interface Terminal extends GrammaticalUnit {

	public boolean test(String text);
	
	public int getLength();
	
	public static class LiteralTerminal implements Terminal {
		
		private final String text;

		private LiteralTerminal(String text) {
			this.text = text;
		}

		@Override
		public boolean test(String text) {
			return this.text.equals(text);
		}

		@Override
		public int getLength() {
			return text.length();
		}
		
		@Override
		public String toString() {
			return String.format("\"%s\"", text);
		}
		
	}
	
	public static class PredicateCharTerminal implements Terminal {
		
		private final Predicate<Character> predicate;

		public PredicateCharTerminal(Predicate<Character> predicate) {
			this.predicate = predicate;
		}

		@Override
		public boolean test(String text) {
			return text != null
				&& text.length() == 1
				&& predicate.test(text.charAt(0));
		}

		@Override
		public int getLength() {
			return 1;
		}
		
	}
	
	public static Terminal newLiteralTerminal(String text) {
		return new LiteralTerminal(text);
	}
	
	public static Terminal newCharacterSetTerminal(CharMatcher charMatcher) {
		return new PredicateCharTerminal(charMatcher::matches);
	}
	
}
