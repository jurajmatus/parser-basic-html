package lexicalanalysis;

@SuppressWarnings("serial")
public class InvalidTokenException extends Exception {

	private final String token;
	
	private final long position;

	public InvalidTokenException(String token, long position) {
		this.token = token;
		this.position = position;
	}

	public String getToken() {
		return token;
	}

	public long getPosition() {
		return position;
	}
	
	@Override
	public String getMessage() {
		return String.format("Invalid token (%s) at position %d", token, position);
	}
	
}
