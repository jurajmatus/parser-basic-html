package parsing;

import java.util.HashMap;
import java.util.Map;

import lexicalanalysis.GrammaticalUnit;

public class NonTerminal implements GrammaticalUnit {

	private static final Map<String, NonTerminal> NT_CACHE = new HashMap<>();
	
	public static NonTerminal withName(String name) {
		return NT_CACHE.computeIfAbsent(name, NonTerminal::new);
	}
	
	private final String name;

	private NonTerminal(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
}
