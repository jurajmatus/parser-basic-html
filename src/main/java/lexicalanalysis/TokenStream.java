package lexicalanalysis;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import static java.util.stream.Collectors.toList;

public class TokenStream implements Closeable, AutoCloseable {
	
	private final static List<Terminal> TERMINALS;
	private final static int MAX_TOKEN_LENGTH;
	
	public final static Terminal WHITESPACE = Terminal.newCharacterSetTerminal("WS", CharMatcher.whitespace());
	public final static Terminal LETTER = Terminal.newCharacterSetTerminal("[a-zA-Z]", CharMatcher.javaLetter());
	public final static Terminal DIGIT = Terminal.newCharacterSetTerminal("[0-9]", CharMatcher.digit());
	public final static Terminal OTHERSYMBOL = Terminal.newCharacterSetTerminal("[.,!- ...]", CharMatcher.anyOf(".,-!/_:'\"|;+-*"));
	
	static {
		Builder<Terminal> builder = Stream.builder();
		
		builder.add(LETTER);
		builder.add(DIGIT);
		builder.add(OTHERSYMBOL);
		builder.add(WHITESPACE);
		
		String allLiteralTokens = "<html>;</html>;<head>;</head>;<title>;</title>;<meta;name=;content=;>;"
								+ "<body>;</body>;<p>;</p>;<table>;</table>;<tr>;</tr>;<td>;</td>;<ul>;"
								+ "</ul>;<ol>;</ol>;<li>;<dl>;</dl>;<dt>;<dd>";
		
		Stream<Terminal> tokens = Stream.concat(builder.build(),
				Splitter.on(';').splitToList(allLiteralTokens).stream().map(Terminal::newLiteralTerminal));
		
		TERMINALS = tokens
				.sorted((t1, t2) -> Integer.compare(t2.getLength(), t1.getLength()))
				.collect(toList());
		
		MAX_TOKEN_LENGTH = TERMINALS.get(0).getLength();
	}

	private final Reader reader;
	
	private int position = 0;

	public TokenStream(Reader reader) {
		this.reader = reader;
	}
	
	public Token read() throws IOException, InvalidTokenException {
		
		char[] buffer = new char[MAX_TOKEN_LENGTH];
		reader.mark(MAX_TOKEN_LENGTH);
		int numRead = reader.read(buffer);
		
		if (numRead < 1) {
			return Token.EOF;
		}
		
		String subStr = null;
		int lastLength = -1;
		
		for (Terminal terminal : TERMINALS) {
			if (lastLength == -1) {
				lastLength = terminal.getLength();
			}
			if (subStr == null || terminal.getLength() < lastLength) {
				subStr = new String(Arrays.copyOf(buffer, terminal.getLength()));
				lastLength = terminal.getLength();
			}
			
			if (terminal.test(subStr)) {
				reader.reset();
				reader.skip(lastLength);
				position += lastLength;
				
				// Whitespace is skipped
				if (terminal == WHITESPACE) {
					return read();
				}
				
				return Token.of(subStr);
			}
		}
		
		throw new InvalidTokenException(new String(buffer), position);
		
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

}
