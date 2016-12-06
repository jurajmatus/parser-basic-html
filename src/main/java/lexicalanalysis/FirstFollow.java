package lexicalanalysis;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import parsing.NonTerminal;
import parsing.Rule;

public class FirstFollow {

	private final Map<Rule, Set<Terminal>> firstForR = new HashMap<>();
	
	private final Map<NonTerminal, Set<Terminal>> firstForNT = new HashMap<>();
	
	private final Map<NonTerminal, Set<Terminal>> follow = new HashMap<>();
	private final Map<NonTerminal, Set<NonTerminal>> followToExpand = new HashMap<>();
	
	private final List<Rule> ruleList;

	private final Map<NonTerminal, List<Rule>> rulesByNT;
	
	public FirstFollow(List<Rule> ruleList) {
		this.ruleList = ruleList;
		rulesByNT = ruleList.stream().collect(groupingBy(Rule::getRuleOf));
		ruleList.forEach(this::getFirstForRule);
		
		addToFollow(ruleList.get(0).getRuleOf(), Terminal.DOLLAR);
		ruleList.forEach(this::findFollow);
		expandFollow();
	}
	
	private Set<Terminal> getFirstForNonTerminal(NonTerminal nt) {
		return firstForNT.computeIfAbsent(nt, this::findFirstForNonTerminal);
	}
	
	private Set<Terminal> findFirstForNonTerminal(NonTerminal nt) {
		List<Rule> rulesForNT = rulesByNT.get(nt);
		if (rulesForNT == null) {
			return Collections.emptySet();
		}
		
		return rulesForNT.stream()
				.flatMap(rule -> getFirstForRule(rule).stream())
				.collect(toSet());
	}
	
	private Set<Terminal> getFirstForRule(Rule rule) {
		return firstForR.computeIfAbsent(rule, this::findFirstForRule);
	}
	
	private Set<Terminal> findFirstForRule(Rule rule) {
		List<GrammaticalUnit> expansion = rule.getExpansion();
		
		if (expansion.isEmpty()) {
			return Stream.of(Terminal.EPSILON).collect(toSet());
		}

		Set<Terminal> all = new HashSet<>();
		for (GrammaticalUnit unit : expansion) {
			if (unit instanceof Terminal) {
				return Stream.of((Terminal) unit).collect(toSet());
			}
			
			if (unit instanceof NonTerminal) {
				if (unit == rule.getRuleOf()) {
					if (!all.contains(Terminal.EPSILON)) {
						return all;
					} else {
						continue;
					}
				}
				
				all.addAll(getFirstForNonTerminal((NonTerminal) unit));
				if (!all.contains(Terminal.EPSILON)) {
					return all;
				}
				
				if (unit != expansion.get(expansion.size() - 1)) {
					all.remove(Terminal.EPSILON);
				}
			}
		}
		
		return all;
		
	}
	
	private void findFollow(Rule rule) {
		List<GrammaticalUnit> expansion = rule.getExpansion();
		
		Set<NonTerminal> current = new HashSet<>();
		for (GrammaticalUnit unit : expansion) {
			
			if (unit instanceof Terminal) {
				current.forEach(nt -> {
					addToFollow(nt, (Terminal) unit);
				});
				current.clear();
			} else if (unit instanceof NonTerminal) {
				Set<Terminal> firstForNext = getFirstForNonTerminal((NonTerminal) unit);
				current.forEach(nt -> {
					addToFollow(nt, firstForNext);
				});
				if (!firstForNext.contains(Terminal.EPSILON)) {
					current.clear();
				}
			}
			
			if (unit instanceof NonTerminal) {
				current.add((NonTerminal) unit);
			}
			
		}
		
		current.forEach(nt -> {
			addToFollow(nt, rule.getRuleOf());
		});
		
	}

	private void addToFollow(NonTerminal nt, NonTerminal toExpand) {
		followToExpand.merge(nt, Stream.of(toExpand).collect(toSet()), Sets::union);
	}

	private void addToFollow(NonTerminal nt, Terminal term) {
		addToFollow(nt, Arrays.asList(term));
	}
	
	private void addToFollow(NonTerminal nt, Collection<Terminal> terms) {
		follow.merge(nt, terms.stream().filter(t -> t != Terminal.EPSILON).collect(toSet()), Sets::union);
	}
	
	private void expandFollow() {
		int i = 0;
		boolean appliedExpand = true;
		while (appliedExpand && (i++) < ruleList.size()) {
			appliedExpand = false;
			for (NonTerminal nt : followToExpand.keySet()) {
				Set<NonTerminal> ntsToExpand = followToExpand.get(nt);
				Set<NonTerminal> toRemove = new HashSet<>();
				for (NonTerminal link : ntsToExpand) {
					if (link != nt) {
						appliedExpand = true;
						Set<Terminal> expandedLink = follow.getOrDefault(link, Collections.emptySet());
						addToFollow(nt, expandedLink);
					}
					if (link == nt || followToExpand.getOrDefault(link, Collections.emptySet())
							.stream().filter(_nt -> _nt != nt).count() == 0) {
						toRemove.add(link);
					}
				}
				followToExpand.put(nt, Sets.difference(ntsToExpand, toRemove));
			}
		}
	}
	
	public String first() {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (Rule rule : ruleList) {
			builder.append(++i + ". " + rule.getRuleOf().getName() + " | {"
				+ getFirstForRule(rule).stream()
					.sorted((t1, t2) -> Integer.compare(t2.getLength(), t1.getLength()))
					.map(t -> t.toString()).collect(joining(", ")) + "}\n");
		}
		return builder.toString();
	}
	
	public String follow() {
		StringBuilder builder = new StringBuilder();
		Set<NonTerminal> handled = new HashSet<>();
		for (Rule rule : ruleList) {
			if (!handled.contains(rule.getRuleOf())) {
				builder.append(rule.getRuleOf().getName() + " | {"
					+ follow.get(rule.getRuleOf()).stream()
						.sorted((t1, t2) -> Integer.compare(t2.getLength(), t1.getLength()))
						.map(t -> t.toString()).collect(joining(", ")) + "}\n");
				handled.add(rule.getRuleOf());
			}
		}
		return builder.toString();
	}
	
	public String report() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%s;%s;%s\n", "RULE", "FIRST", "FOLLOW"));
		Set<NonTerminal> handled = new HashSet<>();
		int i = 0;
		for (Rule rule : ruleList) {
			
			String first = "{" + getFirstForRule(rule).stream()
					.sorted((t1, t2) -> Integer.compare(t2.getLength(), t1.getLength()))
					.map(t -> t.toString()).collect(joining(", ")) + "}";
			
			String follow;
			if (!handled.contains(rule.getRuleOf())) {
				follow = "{" + this.follow.getOrDefault(rule.getRuleOf(), Collections.emptySet()).stream()
							.sorted((t1, t2) -> Integer.compare(t2.getLength(), t1.getLength()))
							.map(t -> t.toString()).collect(joining(", ")) + "}";
				handled.add(rule.getRuleOf());
			} else {
				follow = "-";
			}
			
			builder.append(String.format("%d. %s;%s;%s\n", ++i, rule.toString(), first, follow));
		}
		return builder.toString();
	}
	
	public Set<Terminal> getTerminalsForRuleApplication(Rule rule) {
		Set<Terminal> ret = new HashSet<>(getFirstForRule(rule));
		if (ret.contains(Terminal.EPSILON)) {
			ret.addAll(follow.getOrDefault(rule.getRuleOf(), Collections.emptySet()));
			ret.remove(Terminal.EPSILON);
		}
		return ret;
	}
	
	public Table<NonTerminal, Terminal, Rule> createRuleTable() {
		Table<NonTerminal, Terminal, Rule> ruleTable = HashBasedTable.create(40, 20);
		for (Rule rule : ruleList) {
			for (Terminal term : getTerminalsForRuleApplication(rule)) {
				Rule old = ruleTable.get(rule.getRuleOf(), term);
				if (old == null || rule.getExpansion().size() > old.getExpansion().size()) {
					ruleTable.put(rule.getRuleOf(), term, rule);
				}
			}
		}
		return ruleTable;
	}
	
}
