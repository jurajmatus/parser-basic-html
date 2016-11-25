import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lexicalanalysis.TokenStream;

public class Program {

	public static void main(String[] args) throws IOException {

		if (args.length < 1) {
			System.out.println("Usage: parser source-file");
			System.exit(1);
		}
		
		Path source = Paths.get(args[0]);
		if (!Files.exists(source)) {
			System.out.println("File was expected as the first argument");
			System.exit(1);
		}
		
		try (TokenStream tokenStream = new TokenStream(Files.newBufferedReader(source))) {
			
		}
		
	}

}
