package Server;

import java.io.Serializable;
import java.util.ArrayList;
import Client.RemoteObjectClient;

public class User implements Serializable{

	private static final long serialVersionUID = 1L;
	private String username;
	private String password;
	private String token;
	private int friendPort;
	private ArrayList<String> sentRequests = null;
	private ArrayList<String> suspendedRequests = null;
	private ArrayList<String> friendList = null;
	private ArrayList<String> followers = null;
	private RemoteObjectClient myRmi = null;
	
	private Boolean feedback;
	
	public User(String username2, String password2) {
		this.username = username2;
		this.password = password2;
		this.friendList = new ArrayList<String>();
		this.followers = new ArrayList<String>();
		this.sentRequests = new ArrayList<String>();
		this.suspendedRequests = new ArrayList<String>();
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
	public ArrayList<String> getSentRequests() {
		return sentRequests;
	}
	public void setSentRequests(ArrayList<String> sentRequests) {
		this.sentRequests = sentRequests;
	}
	public ArrayList<String> getSuspendedRequests() {
		return suspendedRequests;
	}
	public void setSuspendedRequests(ArrayList<String> suspendedRequests) {
		this.suspendedRequests = suspendedRequests;
	}
	public ArrayList<String> getFriendList() {
		return friendList;
	}
	public void setFriendList(ArrayList<String> friendList) {
		this.friendList = friendList;
	}
	public synchronized Boolean getFeedback() {
		return feedback;
	}
	public synchronized void setFeedback(Boolean feedback) {
		this.feedback = feedback;
	}
	public ArrayList<String> getFollowers() {
		return followers;
	}
	public void setFollowers(ArrayList<String> followers) {
		this.followers = followers;
	}
	public RemoteObjectClient getMyRmi() {
		return myRmi;
	}
	public void setMyRmi(RemoteObjectClient myRmi) {
		this.myRmi = myRmi;
	}
	
	
}

