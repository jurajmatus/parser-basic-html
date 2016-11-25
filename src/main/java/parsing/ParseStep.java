package parsing;

import lexicalanalysis.Token;

public class ParseStep {
	
	private String text;
	
	private ParseStep(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public boolean isDone() {
		return false;
	}

	public static ParseStep accept() {
		return new ParseStep("# ; $ -> A") {
			@Override
			public boolean isDone() {
				return true;
			}
		};
	}

	public static ParseStep consume(Token token) {
		return new ParseStep(String.format("%s ; %s -> Z", token.getText(), token.getText()));
	}

	public static ParseStep expand(Rule rule, Token token) {
		return new ParseStep(String.format("%s ; %s -> Rule: %s", rule.getRuleOf().getName(), token.getText(), rule.toString()));
	}
	
}
