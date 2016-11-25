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
		 */
		
		Rule.define("htmldocument", t("<html>"), n("documenthead"), n("documentbody"), t("</html>"))
			.register(ruleTable, ruleList, t("<html>"));
		Rule.define("documenthead", t("<head>"), n("headertags"), t("</head>"))
			.register(ruleTable, ruleList, t("<head>"));
		Rule.define("headertags", n("headertag"), n("headertags"))
			.register(ruleTable, ruleList, t("<title>"), t("<meta>"));
		Rule.define("headertags")
			.register(ruleTable, ruleList, t("</head>"));
		Rule.define("headertag", n("titletag"))
			.register(ruleTable, ruleList, t("<title>"));
		Rule.define("headertag", n("metatag"))
			.register(ruleTable, ruleList, t("<meta>"));
		Rule.define("titletag", t("<title>"), n("content"), t("</title>"))
			.register(ruleTable, ruleList, t("<title>"));
		Rule.define("metatag", t("<meta"), t("name="), n("word"), t("content="), n("word"), t(">"))
			.register(ruleTable, ruleList, t("<meta"));
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
