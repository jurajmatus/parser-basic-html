package parsing;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Table;

import static java.util.stream.Collectors.joining;

import lexicalanalysis.GrammaticalUnit;
import lexicalanalysis.Terminal;

public class Rule {
	
	public static Rule define(String nonterminalName, GrammaticalUnit... expansion) {
		return new Rule(NonTerminal.withName(nonterminalName), Arrays.asList(expansion));
	}

	private final NonTerminal ruleOf;
	
	private final List<GrammaticalUnit> expansion;

	private Rule(NonTerminal ruleOf, List<GrammaticalUnit> expansion) {
		this.ruleOf = ruleOf;
		this.expansion = expansion;
	}

	public List<GrammaticalUnit> getExpansion() {
		return expansion;
	}

	public NonTerminal getRuleOf() {
		return ruleOf;
	}
	
	public void register(Table<NonTerminal, Terminal, Rule> ruleTable, List<Rule> ruleList, Terminal... forTerminals) {
		ruleList.add(this);
		for (Terminal terminal : forTerminals) {
			ruleTable.put(ruleOf, terminal, this);
		}
	}
	
	@Override
	public String toString() {
		return ruleOf.toString() + " -> " + expansion.stream().map(gu -> gu.toString()).collect(joining(" "));
	}
	
}
