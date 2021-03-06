import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lexicalanalysis.FirstFollow;
import lexicalanalysis.InvalidTokenException;
import lexicalanalysis.TokenStream;
import parsing.ParseException;
import parsing.ParseStep;
import parsing.Parser;

public class Program {

	public static void main(String[] args) throws IOException {

		if (args.length < 1) {
			System.out.println("Usage: parser source-file | --command");
			System.exit(1);
		}
		
		String cmd = args[0];
		if (cmd.equals("--find-first-follow")) {
			
			FirstFollow firstFollow = Parser.getFirstFollow();
			System.out.println(firstFollow.report());
			
		} else {
		
			Path source = Paths.get(args[0]);
			if (!Files.exists(source)) {
				System.out.println("File was expected as the first argument");
				System.exit(1);
			}
			
			try (Parser parser = new Parser(new TokenStream(Files.newBufferedReader(source)))) {
				ParseStep ps = null;
				do {
					try {
						ps = parser.step();
						System.out.println(ps.toString());
					} catch (ParseException | InvalidTokenException e) {
						ps = null;
						System.err.println("Error in parsing:");
						System.err.println("   " + e.getMessage());
					}
				} while (ps == null || !ps.isDone());
			}
		
		}
		
	}

}
