package lexicalanalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class TestTokenStream {

	private TokenStream tokenStream;
	
	@Before
	public void setUp() throws IOException {
		tokenStream = new TokenStream(Files.newBufferedReader(Paths.get("src/test/resources/test1.html")));
	}
	
	@Test
	public void dump() throws Exception {
		Token token;
		do {
			token = tokenStream.read();
			System.out.println(token.toString());
		} while (!token.equals(Token.EOF));
	}
	
}
