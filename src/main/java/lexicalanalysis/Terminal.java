package lexicalanalysis;

import java.util.function.Predicate;

import com.google.common.base.CharMatcher;

public interface Terminal extends GrammaticalUnit {
	
	public static final Terminal EPSILON = new Terminal() {
		@Override
		public boolean test(String text) {
			return false;
		}
		@Override
		public int getLength() {
			return -1;
		}
		public String toString() {
			return "Ɛ";
		};
	};
	
	public static final Terminal DOLLAR = new Terminal() {
		@Override
		public boolean test(String text) {
			return false;
		}
		@Override
		public int getLength() {
			return -1;
		}
		public String toString() {
			return "$";
		};
	};

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
		
		private final String name;

		public PredicateCharTerminal(String name, Predicate<Character> predicate) {
			this.name = name;
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
		
		@Override
		public String toString() {
			return name;
		}
		
	}
	
	public static Terminal newLiteralTerminal(String text) {
		return new LiteralTerminal(text);
	}
	
	public static Terminal newCharacterSetTerminal(String name, CharMatcher charMatcher) {
		return new PredicateCharTerminal(name, charMatcher::matches);
	}
	
}
