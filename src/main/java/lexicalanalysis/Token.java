package lexicalanalysis;

import java.util.HashMap;
import java.util.Map;

public class Token {
	
	public static final Token EOF = new Token("") {
		public String toString() {
			return "__EOF__";
		};
	};
	public static final Token WS = new Token("") {
		public String toString() {
			return "__WS__";
		}
	};
	
	private static final Map<String, Token> TOKEN_CACHE = new HashMap<>();
	
	public static Token of(String text) {
		if (text.length() == 0) {
			return EOF;
		}
		return TOKEN_CACHE.computeIfAbsent(text, Token::new);
	}

	private final String text;

	private Token(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		}
		// Zero-length tokens are special - they need to be identical to equal
		else if (text.length() == 0 || !text.equals(other.text))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
}
