package Client;

import java.io.Serializable;

public class PayloadTCPFromClientToServer implements Serializable{

	/**
	 * 
	 */
	private String username;
	private String password;
	private String operation;
	private String friendUsr;
	private String token;
	private String message;
	private int friendPort;
	private static final long serialVersionUID = 1L;
	
	public PayloadTCPFromClientToServer(String username, String password, String operation,String friendUsername,String token,int friendPort,String message) {
		this.username = username;
		this.password = password;
		this.operation = operation;
		this.friendUsr = friendUsername;
		this.token = token;
		this.friendPort = friendPort;
		this.message = message;
	}
	public PayloadTCPFromClientToServer() {
		
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getFriendUsr() {
		return friendUsr;
	}
	public void setFriendUsr(String friendUsr) {
		this.friendUsr = friendUsr;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public int getFriendPort() {
		return friendPort;
	}
	public void setFriendPort(int friendPort) {
		this.friendPort = friendPort;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
