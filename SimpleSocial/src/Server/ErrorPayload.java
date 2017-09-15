package Server;

import java.io.Serializable;

public class ErrorPayload implements Serializable{

	private static final long serialVersionUID = 1L;
	private String errorMessage;
	public ErrorPayload(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
