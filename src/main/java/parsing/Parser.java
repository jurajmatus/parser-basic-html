package parsing;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import lexicalanalysis.GrammaticalUnit;
import lexicalanalysis.InvalidTokenException;
import lexicalanalysis.Terminal;
import lexicalanalysis.Token;
import lexicalanalysis.TokenStream;

public class Parser implements Closeable, AutoCloseable {
	
	private static Terminal t(String text) {
		return Terminal.newLiteralTerminal(text);
	}
	
	private static NonTerminal n(String name) {
		return NonTerminal.withName(name);
	}
	
	private final static Table<NonTerminal, Terminal, Rule> ruleTable = HashBasedTable.create(40, 20);
	private final static List<Rule> ruleList = new ArrayList<>();
	static {
		
		/*
		 * Definition of non-epsilon rules:
		 * Rule.define(nonterminalname, terminal1, nonterminal1, terminal2, ...)
		 * 		.register(rulaTable, ruleList, first1, first2, ...);
		 * 
		 * Definition of epsilon rules:
		 * Rule.define(nonterminalname)
		 * 		.register(rulaTable, ruleList, follow1, follow2, ...);
		 * 
		 * example:
		 * 	S -> A B c d ... FIRST(1) = {"f", "g"}
		 * 	Rule.define("S", n("A"), n("B"), t("c"), t("d"))
		 * 		.register(ruleTable, ruleList, t("f"), t("g"));
		 * 
		 * terminals letter, digit and othersymbol are defined as static fields (not t("letter"), ...):
		 * TokenStream.LETTER
		 * TokenStream.DIGIT
		 * TokenStream.OTHERSYMBOL
		 */
		
		Rule.define("htmldocument", t("<html>"), n("documenthead"), n("documentbody"), t("</html>"))
			.register(ruleTable, ruleList, t("<html>"));
		Rule.define("documenthead", t("<head>"), n("headertags"), t("</head>"))
			.register(ruleTable, ruleList, t("<head>"));
		Rule.define("headertags", n("headertag"), n("headertags"))
			.register(ruleTable, ruleList, t("<title>"), t("<meta"));
		Rule.define("headertags")
			.register(ruleTable, ruleList, t("</head>"));
		Rule.define("headertag", n("titletag"))
			.register(ruleTable, ruleList, t("<title>"));
		Rule.define("headertag", n("metatag"))
			.register(ruleTable, ruleList, t("<meta"));
		Rule.define("titletag", t("<title>"), n("content"), t("</title>"))
			.register(ruleTable, ruleList, t("<title>"));
		Rule.define("metatag", t("<meta"), t("name="), n("word"), t("content="), n("word"), t(">"))
			.register(ruleTable, ruleList, t("<meta"));
		Rule.define("documentbody", t("<body>"), n("bodytags"), t("</body>"))
				.register(ruleTable, ruleList, t("<body>"));
		Rule.define("bodytags", n("bodytag"), n("bodytags"))
				.register(ruleTable, ruleList, t("<table>"),t("<ul>"),t("<ol>"),t("<dl>"),t("<p>"),
						TokenStream.LETTER, TokenStream.DIGIT, TokenStream.OTHERSYMBOL);
		Rule.define("bodytags")
				.register(ruleTable, ruleList, t("</body>"),t("</html>"),t("</p>"),t("</td>"));
		Rule.define("bodytag", n("table"))
				.register(ruleTable, ruleList, t("<table>"));
		Rule.define("bodytag", n("list"))
				.register(ruleTable, ruleList, t("<ul>"),t("<ol>"),t("<dl>"));
		Rule.define("bodytag", n("paragraph"))
				.register(ruleTable, ruleList, t("<p>"));
		Rule.define("bodytag", n("content"))
				.register(ruleTable, ruleList,TokenStream.LETTER, TokenStream.DIGIT, TokenStream.OTHERSYMBOL,
						t("<table>"),t("<ul>"),t("<ol>"),t("<dl>"),t("<p>"),t("</body>"),t("</html>"),t("</p>"),t("</td>"),
						t("<dt>"), t("<dd>"), t("<li>"), t("</ul>"), t("</ol>"));
		Rule.define("paragraph", t("<p>"),n("bodytags"),n("paragraphend"))
				.register(ruleTable, ruleList,t("<p>"));
		Rule.define("paragraphend", t("</p>"))
				.register(ruleTable, ruleList,t("</p>"));
		Rule.define("paragraphend")
				.register(ruleTable, ruleList, t("<table>"),t("<ul>"),t("<ol>"),t("<dl>"),t("<p>"),t("</body>"),t("</html>"),t("</p>"),t("</td>"));
		Rule.define("table",t("<table>"),n("tablerows"), t("</table>"))
				.register(ruleTable, ruleList, t("<table>"));
		Rule.define("tablerows",n("tablerow"),n("tablerows"))
				.register(ruleTable, ruleList, t("<tr>"));
		Rule.define("tablerows")
				.register(ruleTable, ruleList, t("</table>"));
		Rule.define("tablerow",t("<tr>"),n("tablecells"),t("</tr>"))
				.register(ruleTable, ruleList, t("<tr>"));
		Rule.define("tablecells",n("tablecell"),n("tablecells"))
				.register(ruleTable, ruleList, t("<td>"));
		Rule.define("tablecells")
				.register(ruleTable, ruleList, t("</tr>"));
		Rule.define("tablecell",t("<td>"),n("bodytags"),n("tablecellend"))
				.register(ruleTable, ruleList, t("<td>"));
		Rule.define("tablecellend",t("</td>"))
				.register(ruleTable, ruleList, t("</td>"));
		Rule.define("tablecellend")
				.register(ruleTable, ruleList, t("<td>"), t("</tr>"));
		Rule.define("list",n("unordered"))
				.register(ruleTable, ruleList, t("<ul>"));
		Rule.define("list",n("ordered"))
				.register(ruleTable, ruleList, t("<ol>"));
		Rule.define("list",n("definitionlist"))
				.register(ruleTable, ruleList, t("<dl>"));
		Rule.define("unordered",t("<ul>"),n("listitems"),t("</ul>"))
				.register(ruleTable, ruleList, t("<ul>"));
		Rule.define("ordered",t("<ol>"),n("listitems"),t("</ol>"))
				.register(ruleTable, ruleList, t("<ol>"));
		Rule.define("listitems",t("<li>"),n("bodytag"),n("listitems"))
				.register(ruleTable, ruleList, t("<li>"));
		Rule.define("listitems")
				.register(ruleTable, ruleList, t("</ul>"),t("</ol>"));
		Rule.define("definitionlist",t("<dl>"),n("defterms"),t("</dl>"))
				.register(ruleTable, ruleList,t("<dl>"));
		Rule.define("defterms",n("defterm"),n("defterms"))
				.register(ruleTable, ruleList,t("<dt>"),t("<dd>"));
		Rule.define("defterms")
				.register(ruleTable, ruleList,t("</dl>"));
		Rule.define("defterm",t("<dt>"),n("bodytag"))
				.register(ruleTable, ruleList,t("<dt>"));
		Rule.define("defterm",t("<dd>"),n("bodytag"))
				.register(ruleTable, ruleList,t("<dd>"));
		Rule.define("content", n("word"))
			.register(ruleTable, ruleList, TokenStream.LETTER, TokenStream.DIGIT, TokenStream.OTHERSYMBOL);
		Rule.define("content")
			.register(ruleTable, ruleList, t("content="), t("</title>"), t("<table>"), t("<ul>"), t("<ol>"),
					t("<dl>"), t("<p>"), t("</body>"), t("</html>"), t("</p>"), t("</td>"), t(">"), t("<dt>"),
					t("<dd>"), t("<li>"), t("</ul>"), t("</ol>"));
		Rule.define("word", n("char"), n("content"))
			.register(ruleTable, ruleList, TokenStream.LETTER, TokenStream.DIGIT, TokenStream.OTHERSYMBOL);
		Rule.define("char", TokenStream.LETTER)
			.register(ruleTable, ruleList, TokenStream.LETTER);
		Rule.define("char", TokenStream.DIGIT)
			.register(ruleTable, ruleList, TokenStream.DIGIT);
		Rule.define("char", TokenStream.OTHERSYMBOL)
			.register(ruleTable, ruleList, TokenStream.OTHERSYMBOL);
	}

	private final TokenStream tokenStream;
	
	private final List<GrammaticalUnit> stack = new ArrayList<>();
	
	private Token lastToken;

	public Parser(TokenStream tokenStream) {
		this.tokenStream = tokenStream;
		stack.add(ruleList.get(0).getRuleOf());
	}
	
	private GrammaticalUnit take() {
		return stack.remove(stack.size() - 1);
	}
	
	private void consume() throws IOException, InvalidTokenException {
		lastToken = tokenStream.read();
	}
	
	private void expand(Rule rule) {
		List<GrammaticalUnit> expansion = rule.getExpansion();
		for (int i = expansion.size() - 1 ; i >= 0; i--) {
			stack.add(expansion.get(i));
		}
	}
	
	public ParseStep step() throws ParseException, IOException, InvalidTokenException {
		
		if (lastToken == null) {
			consume();
		}
		Token token = lastToken;
		
		boolean terminalMode = stack.isEmpty();
		GrammaticalUnit bottomOfStack = null;
		if (!terminalMode) {
			bottomOfStack = take();
			terminalMode = bottomOfStack instanceof Terminal;
		}
		
		if (terminalMode) {			
			if (token.equals(Token.EOF)) {
				// # ; $ (A)
				if (bottomOfStack == null) {
					return ParseStep.accept();
				}
				// # ; term (X)
				else {
					throw new ParseException("Unexpected end of file at position " + tokenStream.getPosition() +  ".");
				}
			}
			
			// term1 ; term1 (Z)
			if (((Terminal) bottomOfStack).test(lastToken.getText())) {
				consume();
				return ParseStep.consume(token);
			}
			// term1 ; term2 (X)
			else {
				throw new ParseException("Unexpected token " + token.toString() + ". Expected " + bottomOfStack.toString() + ".");
			}
		}

		for (Entry<Terminal, Rule> entry : ruleTable.row((NonTerminal) bottomOfStack).entrySet()) {
			Rule rule = entry.getValue();
			// non-term ; term
			if (entry.getKey().test(token.getText())) {
				expand(entry.getValue());
				return ParseStep.expand(rule, token);
			}
		}
		
		throw new ParseException("No rule for nonterminal " + bottomOfStack.toString() + " and token " + token.toString());
		
	}

	@Override
	public void close() throws IOException {
		tokenStream.close();
	}
	
}
