package Server;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import Client.PayloadTCPFromClientToServer;

import static Const.Constants.friendHostname;
import static Const.Constants.backUpFile;

//The Worker Thread, the thread that does all the operations
public class ServerWorker implements Runnable{

	Socket client;
	ObjectInputStream in;
	ObjectOutputStream out;
	
	//constructor
	public ServerWorker(Socket client) {
		this.client=client;
	}

	//run function
	public void run() {

		//Opening streams and reading the messages
		System.out.println("Working with client");
		try {
			in= new ObjectInputStream(client.getInputStream());
			out= new ObjectOutputStream(client.getOutputStream());
			PayloadTCPFromClientToServer msg = new PayloadTCPFromClientToServer();
			msg = (PayloadTCPFromClientToServer) in.readObject();
			checkMessage(msg);

		}catch (IOException e) {
			System.err.println("Error: ServerClientHandler "+ e.getMessage());	
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally{
			try{
				client.close();
			} catch(IOException e){ System.err.println("Closing error: "+ e.getMessage());}
		}
		
		
	}//run function ends
	
	
	//checkMessage function
	private void checkMessage(PayloadTCPFromClientToServer msg) {
		
		//if the message is null, something went wrong on the TCP connection and so it sends back an error payload.
		//the error payload is handled on the client's side.
		if(msg == null){
			System.err.println("ERROR: Message received from client is NULL");
			try {
				ErrorPayload err = new ErrorPayload("Message not Received");
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}//end of if

		//checking what operation it needs to do
		switch (msg.getOperation()) {
			case "register" : register(msg.getPassword(),msg.getUsername());
							  break;
			
			case "login" : login(msg.getPassword(),msg.getUsername(),msg.getFriendPort());
						   break;
						   
			case "request" : friendRequest(msg.getPassword(),msg.getUsername(),msg.getFriendUsr(),msg.getToken());
							 break;
							 
			case "getSuspended" : sendSuspendedList(msg.getUsername(),msg.getToken());
			 				      break;
			 				 
			case "confirmRequest" : confirmRequest(msg.getFriendUsr(),msg.getToken());
			 					    break; 
			 					    
			case "deleteRequest" : deleteRequest(msg.getFriendUsr(),msg.getToken());
			  						break; 
			  						
			case "getFriendsList" : friendsList(msg.getUsername(),msg.getToken());
									break;
									
			case "searchUsr" : searchList(msg.getFriendUsr(),msg.getToken());
								break;
			
			case "publish" : publishMsg(msg.getUsername(),msg.getToken(),msg.getMessage());
								break;
			
			case "logout" : logout(msg.getUsername(),msg.getToken());
							break;
		}
		
		
	}//ends checkMessage

	//register function
	//Reads from the TCP connection and sends back an answer, which can be an error payload or a good payload.
	private void register(String password, String username) {
	
		User t = new User(username,password);
		//check to see if it doesn't already exist
		if(SocialServer.registeredUsers.containsKey(username)){
			//sending error message
			try {
				ErrorPayload err = new ErrorPayload("User already registered!");
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			//send confirm message
			try {	
				PayloadTCPFromServerToClient smsg = new PayloadTCPFromServerToClient();
				smsg.setMessage("confirm");//just a message, doesn't do anything
				out.writeObject(smsg);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//add the new user
			SocialServer.registeredUsers.put(username, t);
		}
		savebackup(username);	
	}//closes register
	
	//create token function
	//token is a string made by currentTime -- and the username
	private String createToken(String usr){
		String t="";
		Long time = System.currentTimeMillis() + (30*60*1000); //30 min token
		t = time + "--" + usr ;
		return t;
	}

	
	//log in function
	private void login(String password, String username,int friendPort) {

		User t = new User(username,password);
		//if the credentials are ok we create the payload and send it to the client
		if( (SocialServer.registeredUsers.containsKey(username)) && (SocialServer.registeredUsers.get(username).getPassword().equals(password)) && (!SocialServer.onlineUsers.containsKey(username)) ){
	
			String tok = createToken(username);//creating the token
			SocialServer.registeredUsers.get(username).setToken(tok);
			SocialServer.registeredUsers.get(username).setFriendPort(friendPort);
			SocialServer.registeredUsers.get(username).setFeedback(true);
			SocialServer.onlineUsers.put(username, t);
			SocialServer.onlineUsers.get(username).setFeedback(true);
			
			try {	
				PayloadTCPFromServerToClient smsg = new PayloadTCPFromServerToClient();
				smsg.setMessage("Logged In!");
				smsg.setToken(tok);
				out.writeObject(smsg);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}// ends if
		//if something was wrong with the credentials
		else{
			//sending error message
			try {
				ErrorPayload err = new ErrorPayload("Invalid Username or Password!");
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	
	}//ends login function
	
	//check token function
	private int checkToken(String token) {
		String temp = token.substring(0,token.indexOf("-"));
		Long t = Long.parseLong(temp);
		User u = null;
		String temp2 = token.substring(token.lastIndexOf("-")+1);
		u = SocialServer.onlineUsers.get(temp2);
		if((t < System.currentTimeMillis()) || (token==null))
			return -1;
		if(u == null)
			return -1;
		u.setFeedback(true);
		return 0;
		
	} //end of checktoken
	
	//friend request function
	private void friendRequest(String password, String username, String friendUsr, String token) {
		
		int t = checkToken(token);
		
		//if token expired send an errorpayload that brings the interface back to the login screen
		if(t==-1) {
			ErrorPayload err = new ErrorPayload("Token Expired");
			SocialServer.onlineUsers.remove(username);
			try {
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		else{
			//check second username and check that he is not trying to connect to himself and that he doesn't try to send it 2 or more times and of course check if they are already friends
			if(!(SocialServer.onlineUsers.containsKey(friendUsr)) || (username.equals(friendUsr)) || (SocialServer.registeredUsers.get(username).getSentRequests().contains(friendUsr)) || 
					SocialServer.registeredUsers.get(username).getFriendList().contains(friendUsr)) {
				ErrorPayload err = new ErrorPayload("Invalid username");
				try {
					out.writeObject(err);
				} catch (IOException e) {
					e.printStackTrace();
				}			
			}
			else{
				System.out.println("Trying to open the TCP connection for my friend");
				try {
					//Connecting to the friends TCP 
					Socket friendSocket = new Socket(friendHostname, SocialServer.registeredUsers.get(friendUsr).getFriendPort());
					friendSocket.setTcpNoDelay(true);
					
					BufferedWriter writerToFriend=null;//i need it to send a message to my friend
					
					if(friendSocket.isConnected()){
						//send confirm to the client that the request has been sent
						PayloadTCPFromServerToClient smsg = new PayloadTCPFromServerToClient();
						smsg.setMessage("Friend request sent!");
						out.writeObject(smsg);
						out.flush();
						//adding the request
						SocialServer.registeredUsers.get(username).getSentRequests().add(friendUsr);
						SocialServer.registeredUsers.get(friendUsr).getSuspendedRequests().add(username);						
						
						//sending a message to my friend
						writerToFriend = new BufferedWriter(new OutputStreamWriter(friendSocket.getOutputStream()));
						writerToFriend.write(username);
						writerToFriend.flush();
					}
					else {
						ErrorPayload err = new ErrorPayload("User Offline");
						try {
							out.writeObject(err);
							out.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}			
					}
					writerToFriend.close();				
					friendSocket.close();
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} // closes the else for user existence
		}	
	}//end of friend request function

	//Sends the suspended list via TCP, it's in the payload from server to client
	private void sendSuspendedList(String username,String token){
		
		//check token
		int t = checkToken(token);
		
		//if token expired send and errorpayload that brings the interface back to the login screen
		if(t==-1) {
			System.out.println("Token Expired!");
			SocialServer.onlineUsers.remove(username);
			ErrorPayload err = new ErrorPayload("Token Expired");
			try {
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		PayloadTCPFromServerToClient smsg = new PayloadTCPFromServerToClient();
		ArrayList<String> requests = new ArrayList<String>();
		requests = SocialServer.registeredUsers.get(username).getSuspendedRequests();
		if(requests == null || requests.isEmpty()){
			System.out.println("Suspended list is empty!");
		}
		smsg.setList(requests);
		try {
			out.writeObject(smsg);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}//ends send suspended list
	
	//confirm request function
	private void confirmRequest(String usr,String token){
		//check token
		int t = checkToken(token);
		
		//if token expired send and errorpayload that brings the interface back to the login screen
		if(t==-1) {
			SocialServer.onlineUsers.remove(usr);
			ErrorPayload err = new ErrorPayload("Token Expired");
			try {
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		PayloadTCPFromServerToClient smsg = new PayloadTCPFromServerToClient();
		smsg.setMessage("Friend request confirmed!");
		//adds the friend to the user who asked
		String temp = token.substring(token.lastIndexOf("-")+1);
		ArrayList<String> arr = new ArrayList<String>();
		arr = SocialServer.registeredUsers.get(temp).getFriendList();
		arr.add(usr);
		SocialServer.registeredUsers.get(temp).setFriendList(arr);
		
		//adds it to the users who's been asked
		SocialServer.registeredUsers.get(usr).getFriendList().add(temp);
		//removes from the suspended requests of this user
		SocialServer.registeredUsers.get(temp).getSuspendedRequests().remove(usr);
		//removes from the sent requests of the asked users
		SocialServer.registeredUsers.get(usr).getSentRequests().remove(temp);	
		try {
			out.writeObject(smsg);
			out.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}//ends confirm request
	
	//delete request function
	private void deleteRequest(String usr,String token){
		//check token
		int t = checkToken(token);
		//if token expired send and errorpayload that brings the interface back to the login screen
		if(t==-1) {
			SocialServer.onlineUsers.remove(usr);
			ErrorPayload err = new ErrorPayload("Token Expired");
			try {
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		PayloadTCPFromServerToClient smsg = new PayloadTCPFromServerToClient();
		smsg.setMessage("Friend request deleted!");
		//removes from the suspended requests of this user
		SocialServer.registeredUsers.get(token.substring(token.lastIndexOf("-")+1)).getSuspendedRequests().remove(usr);
		//removes from the suspended requests of the asked users
		SocialServer.registeredUsers.get(usr).getSentRequests().remove(token.substring(token.lastIndexOf("-")+1));
		try {
			out.writeObject(smsg);
			out.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}//ends delete request
	
	//friends list function send the friendlist in the payload.
	private void friendsList(String usr, String token){
		//check token
		int t = checkToken(token);
		//if token expired send and errorpayload that brings the interface back to the login screen
		if(t==-1) {
			SocialServer.onlineUsers.remove(usr);
			ErrorPayload err = new ErrorPayload("Token Expired");
			try {
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		PayloadTCPFromServerToClient smsg = new PayloadTCPFromServerToClient();
		ArrayList<String> friends = SocialServer.registeredUsers.get(usr).getFriendList();
		ArrayList<String> temp2 = new ArrayList<String>();
		if(friends == null || friends.isEmpty()){
			try {
				out.writeObject(smsg);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!friends.isEmpty()) {
			for(String temp : friends){
				if(SocialServer.onlineUsers.containsKey(temp)){
					temp2.add(temp+"--Online");
				}
				else
					temp2.add(temp+"--Offline");
			}
			smsg.setList(temp2);
			smsg.setMessage("Friend List sent!");
			try {
				out.writeObject(smsg);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}//ends friendsList function
	
	//search user function
	//sends the found users list back to the client via payload and TCP.
	private void searchList(String u,String token){
		//check token
		int t = checkToken(token);
		
		//if token expired send and errorpayload that brings the interface back to the login screen
		if(t==-1) {
			SocialServer.onlineUsers.remove(token.substring(token.lastIndexOf("-")+1));
			System.out.println("ServerWorker: Client Token Expired!");
			ErrorPayload err = new ErrorPayload("Token Expired");
			try {
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		PayloadTCPFromServerToClient smsg = new PayloadTCPFromServerToClient();
		ArrayList<String> foundUsers = new ArrayList<String>();
		for(String temp : SocialServer.registeredUsers.keySet()){
			if(temp.contains(u)){
				foundUsers.add(temp);
			}
		}
		smsg.setList(foundUsers);
		smsg.setMessage("Searched user list sent");
		try {
			out.writeObject(smsg);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} //ends search user function
	
	//publish message function
	//Gets the message via TCP and sends it to all the followers via RMI
	//if a follower is offline the RMI won't work so it will save the message into a file, every user has it's file (MESSAGES_username)
	//Finally it sends a confirmation message back to the client.
	private void publishMsg(String usrname,String token,String pmsg){
		//check token
		int t = checkToken(token);		
		//if token expired send an errorpayload that brings the interface back to the login screen
		if(t==-1) {
			SocialServer.onlineUsers.remove(usrname);
			ErrorPayload err = new ErrorPayload("Token Expired");
			try {
				out.writeObject(err);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		//sending messages to followers
		for(String s: SocialServer.registeredUsers.get(usrname).getFollowers()){
			User u = SocialServer.registeredUsers.get(s);
			String msgg = "From "+usrname+": "+pmsg;
			try {
				if(u.getMyRmi() == null){//follower is offline.
					System.out.println("Hey there i'm offline: "+u.getUsername());
					saveMessages(u.getUsername(),msgg);
				}
				else 
					u.getMyRmi().message(usrname, msgg, s);
			} catch (RemoteException e) {
				e.printStackTrace();
			}//aggiungi null exception
			
		}
		//sending confirm through TCP
		PayloadTCPFromServerToClient smsg = new PayloadTCPFromServerToClient();
		smsg.setMessage("Message published!");
		try {
			out.writeObject(smsg);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}//end of publish msg function
	
	//logout function
	private void logout(String username,String token){
		//i don't check token, it doesn't make a lot of sense checking the token when u want to log out, because it logs out anyways
		System.out.println("Client "+username+" logged out.");
		SocialServer.onlineUsers.remove(username);
		SocialServer.registeredUsers.get(username).setToken(null);
		SocialServer.registeredUsers.get(username).setFeedback(false);
		savebackup(username);
	}//end of logout function
	
	//save messages on file
	//this file will be loaded when a user logs in, this part is done in the Client
	private void saveMessages(String username,String msg){
		String filename = "Messages_"+username;
		try {
			FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw);
		    out.println(msg);
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	//save backup function
	private static void savebackup(String u) {
		//write the registered users HashMap into the backup file
		try {
			FileOutputStream fileout = new FileOutputStream(backUpFile);
			ObjectOutputStream out = new ObjectOutputStream(fileout);
			out.writeObject(SocialServer.registeredUsers);
			out.close();
			fileout.close();
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
