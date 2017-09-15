package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import javax.swing.DefaultListModel;

import Server.ErrorPayload;
import Server.PayloadTCPFromServerToClient;
import Server.RemoteObjectServer;

import static Const.Constants.serverHostname;
import static Const.Constants.ServerSocketTCP;
import static Const.Constants.RMIport;

public class SocialClient {

	Socket serverSocket = null;
	ServerSocket friendSocket = null;
	String usr;
    String psw;
    String token;
    ObjectInputStream in;
    ObjectOutputStream out;
	int tcpConnectionFlag = 0;
	Thread a = null;
	KeepAliveClientThread runner = null;
	RemoteObjectClientImpl roc = null;
	RemoteObjectServer followersMng;
	String messagesToRead = null;
	
	
    public SocialClient (String username,String password,Gui gui)
    {
     	this.usr=username;
     	this.psw=password;  
     	roc = new RemoteObjectClientImpl(gui);

    }
    
    //TCP Connection with Server
    public void connectTCP(){
    	
    	System.out.println ("Attemping to connect to host " +serverHostname + " on port 10008.");
    	
    	try {
            serverSocket = new Socket(serverHostname, ServerSocketTCP);
            System.out.println("Connected.");
            tcpConnectionFlag = 1;
        }catch (SocketException e) {
                System.err.println("SocketException here in GUI connect");
                System.exit(1); 
        } catch (UnknownHostException e) {
            System.err.println("Don't know the host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + serverHostname);
            System.exit(1);
        }
    	
    }//closes connectTCP
    
    //TCP Disconnection 
    public void disconnectTCP(){
    	
    	System.out.println("User "+this.usr+" is disconnecting...");
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    //TCP streams open function
    public void openStreams(){
    	System.out.println("Opening streams");
    	try {
    		 out= new ObjectOutputStream(serverSocket.getOutputStream());
    		 in= new ObjectInputStream(serverSocket.getInputStream());
		} catch (SocketException e) {	
			System.out.println("SocketException in openStreams");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();}
		
    }//closes openStreams function

    //TCP streams close function
    public void closeStreams(){
    	System.out.println("Closing streams");
    	try {
			out.close();
			in.close();
		} catch (SocketException e) {
			System.out.println("SocketException in closeStreams");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();}
		
    }//closes closeStreams function
    
    //The registration function
    //The client sends a payload to the server via TCP connection.
    //It waits for an answer and if it's an ErrorPayload then something went wrong and the operation won't be done.
    //Otherwise everything goes as planned and the Client receives the message that the server registered the client with success.
    public int register(){
    	int ris=0;
    	connectTCP();
    	openStreams();
    	
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"register",null,"",0,null);
    	
    	try{
    		//sending the message to the server
    		out.writeObject(p);
    		
    		//reading the answer from the server
    		Object m = (Object) in.readObject();
    		if(m instanceof ErrorPayload){
    			ris=-1;
    		}
    		if(m instanceof PayloadTCPFromServerToClient){
    			ris=0;
        		System.out.println("I read the message: "+((PayloadTCPFromServerToClient) m).getMessage());
    		}

    		
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	
    	
    	closeStreams();
    	disconnectTCP();
    	return ris;
    	
    }//ends registration
    
    
    //log in function
    //Same operations as the registration function above, the only difference is that here it receives a token that has to be saved and controlled from now on.
    //It also starts some key threads. Like the keep alive thread that can only be started once the user has logged in correctly.
    //Or the friend request thread. In addition there is the RMI part with it's controls and the addCallback.
    public int login(){
 
    	runner = new KeepAliveClientThread(this.usr);
    	a = new Thread(runner);
    	a.setDaemon(true);
    	a.start();
    	
    	int ris = 0;
    	int friendPort = 0;
    	try {
    		friendSocket = new ServerSocket(0);
    		friendPort = friendSocket.getLocalPort();
    	} catch (IOException e1) {
			e1.printStackTrace();
		}
        Thread t = new Thread(new FriendRequestThread(friendSocket,friendPort));
        t.setDaemon(true);
        t.start(); 

    	connectTCP();
    	openStreams();
    	
    	
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"login",null,"",friendPort,null);
    	
    	try{
    		//sending the message to the server
    		out.writeObject(p);
    		
    		//reading the answer from the server
    		Object m = (Object) in.readObject();
    		if(m instanceof ErrorPayload){
    			ris=-1;
    		}
    		if(m instanceof PayloadTCPFromServerToClient){
    			this.token = ((PayloadTCPFromServerToClient) m).getToken();
        		System.out.println("I read the message: "+((PayloadTCPFromServerToClient) m).getMessage());
    		}
    		
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			closeStreams();
	    	disconnectTCP();
		}
    	//RMI
    	if(ris != -1){
	    	try {
	    		RemoteObjectClient userStub = (RemoteObjectClient) UnicastRemoteObject.exportObject(roc,0);
	        	if(followersMng == null)
	        		followersMng = (RemoteObjectServer) LocateRegistry.getRegistry(RMIport).lookup(RemoteObjectServer.OBJECT_NAME);     	
	        	int temp2 = followersMng.addCallback(this.usr, userStub);
	        	if(temp2 == -1)
	        		UnicastRemoteObject.unexportObject(roc, true);
	        	
	        }catch (RemoteException e){
	        	System.out.println("RMIClient error:" +e.getMessage());
	        	ris = -1;
	        }catch (NotBoundException e){
	        	System.out.println("Subscribers manager not available :" +e.getMessage());
	        	ris = -1;
	        }
    	}
    	return ris;
    }//ends log in
    
    //friendRequest function
    public int friendRequestC(String friendUsername){
    	int ris = 0;
    	connectTCP();
    	openStreams();
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"request",friendUsername,this.token,0,null);
    	
    	try{
    		//sending the message to the server
    		out.writeObject(p);
    		//reading the answer from the server
    		Object m = (Object) in.readObject();
    		if(m instanceof ErrorPayload){
    			if(((ErrorPayload) m).getErrorMessage().equals("Token Expired")){ //checking token error
    				this.token= null;
    				terminateThread();
    				ris = -2;
    			}else{
    				ris = -1;
    			}
    		}
    		if(m instanceof PayloadTCPFromServerToClient){
        		System.out.println("I read the message: "+((PayloadTCPFromServerToClient) m).getMessage());
    		}

    		
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	
    	
    	closeStreams();
    	disconnectTCP();
    	return ris;
    } //ends friendRequest function
    
    //support function convertToDefList
    //Because Gui needs a DefaultListModel in the component of it's frame.
    private DefaultListModel<String> convertToDefList(ArrayList<String> arrayList) {
		DefaultListModel<String> list = new DefaultListModel<String>();
		for (String value : arrayList)
		{
		    list.addElement(value);
		}
		return list;	
	}//end of the support function
    
    
    
    //get suspended list function
    public DefaultListModel<String> getSuspendedList() {
    	
    	connectTCP();
    	openStreams();
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"getSuspended",null,this.token,0,null);
    	DefaultListModel<String> list = new DefaultListModel<String>();
    	try{
    		//sending the message to the server
    		out.writeObject(p);
    		//reading the answer from the server
    		Object m = (Object) in.readObject();
    		if(m instanceof ErrorPayload){
    			if(((ErrorPayload) m).getErrorMessage().equals("Token Expired")){ //checking token error
    				this.token= null;
    				terminateThread();
    				return null;
    			}else{
    				return null;
    			}
    		}
    		if(m instanceof PayloadTCPFromServerToClient){
        		//System.out.println("I read the message: "+((PayloadTCPFromServerToClient) m).getMessage());
        		if(((PayloadTCPFromServerToClient) m).getList() == null || ((PayloadTCPFromServerToClient) m).getList().isEmpty()){
        			list.addElement("No suspended requests found!");
        		}
        		else
        			list = convertToDefList(((PayloadTCPFromServerToClient) m).getList());
    		}
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	
    	closeStreams();
    	disconnectTCP();
    	return list;
    	
    }//end of get suspended list function
    
    //confirm request function
    public int confirmRequest(String susr){
    	connectTCP();
    	openStreams();
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"confirmRequest",susr,this.token,0,null);
    	try{
    		out.writeObject(p);
    		Object m = (Object) in.readObject();
    		if(m instanceof ErrorPayload){
    			if(((ErrorPayload) m).getErrorMessage().equals("Token Expired")){ //checking token error
    				this.token= null;
    				terminateThread();
    				return -2;
    			}
    		}
			else
        		System.out.println("I read the message: "+((PayloadTCPFromServerToClient) m).getMessage());//confirm message
	
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	
    	closeStreams();
    	disconnectTCP();
    	return 0;
    }//ends confirm request
    
    //delete request function
    public int deleteRequest(String susr){
    	int ris = 0;
    	connectTCP();
    	openStreams();
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"deleteRequest",susr,this.token,0,null);
    	try{
    		//sending the message to the server
    		out.writeObject(p);
    		//reading the answer from the server
    		Object m = (Object) in.readObject();
    		if(m instanceof ErrorPayload){
    			if(((ErrorPayload) m).getErrorMessage().equals("Token Expired")){ //checking token error
    				this.token= null;
    				terminateThread();
    				return -2;
    			}
    		}
			else
        		System.out.println("I read the message: "+((PayloadTCPFromServerToClient) m).getMessage());//mconfirm message
	
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	
    	closeStreams();
    	disconnectTCP();    	
    	return ris;
    }//ends delete request
    
    //get friends list function
    public DefaultListModel<String> getFriendsList(){
    	connectTCP();
    	openStreams();
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"getFriendsList",null,this.token,0,null);
    	DefaultListModel<String> list = new DefaultListModel<String>();
    	try{
    		//sending the message to the server
    		out.writeObject(p);
    		//reading the answer from the server
    		Object m = (Object) in.readObject();
    		if(m instanceof ErrorPayload){
    			if(((ErrorPayload) m).getErrorMessage().equals("Token Expired")){ //checking token error
    				this.token= null;
    				terminateThread(); //Ends the keepalive thread-- this happens in every method that checks the token
    				return null;
    			}
    		}
    		if(m instanceof PayloadTCPFromServerToClient){
    			if(((PayloadTCPFromServerToClient) m).getList() == null || ((PayloadTCPFromServerToClient) m).getList().isEmpty()){
        			list.addElement("No friends found :(!");
        		}
        		else
        			list = convertToDefList(((PayloadTCPFromServerToClient) m).getList());
    		}
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	
    	closeStreams();
    	disconnectTCP();
    	return list;
    } //end of get friends list
    
    //searchUser function
    public DefaultListModel<String> searchUser(String susr){
    	connectTCP();
    	openStreams();
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"searchUsr",susr,this.token,0,null);
    	DefaultListModel<String> list = new DefaultListModel<String>();
    	try{
    		//sending the message to the server
    		out.writeObject(p);
    		//reading the answer from the server
    		Object m = (Object) in.readObject();
    		if(m instanceof ErrorPayload){
    			if(((ErrorPayload) m).getErrorMessage().equals("Token Expired")){ //checking token error
    				this.token = null;
    				terminateThread();
    				return null;
    			}
    		}
    		else
	    		if(m instanceof PayloadTCPFromServerToClient){
	        		System.out.println("I read the message: "+((PayloadTCPFromServerToClient) m).getMessage());
	        		if(!(((PayloadTCPFromServerToClient) m).getList().isEmpty()))
	        			list = convertToDefList(((PayloadTCPFromServerToClient) m).getList());
	        		else{
	        			System.out.println("List is empty");
	        			list.add(0, "No user was found!");
	        		}
	    		}

    		
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	closeStreams();
    	disconnectTCP();
    	return list;
    }// end of search user function
    
    //publish msg function
    public int publish(String pmsg){
    	int ris = 0;
    	connectTCP();
    	openStreams();
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"publish",null,this.token,0,pmsg);
    	try{
    		//sending the message to the server
    		out.writeObject(p);
    		//reading the answer from the server
    		Object m = (Object) in.readObject();
    		if(m instanceof ErrorPayload){
    			if(((ErrorPayload) m).getErrorMessage().equals("Token Expired")){ //checking token error
    				this.token= "";
    				terminateThread();
    				return -2;
    			}
    			else //in case something went wrong with the RMI i can just send an errorpayload
    				return -1;
    		}
    		//this is only for confirmation, everything is done by the server.
    		if(m instanceof PayloadTCPFromServerToClient){
        		System.out.println("I read the message: "+((PayloadTCPFromServerToClient) m).getMessage());
    		}	
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	closeStreams();
    	disconnectTCP();
    	return ris;
    }//end of publish message
    
    //subscription function
    //Here everything goes through RMI so no ConnectTCP or OpenStreams
    //Even the token check is done locally
    public int subscribe(String text) {
    	int t = localTokenCheck(this.token);
    	int ris = 0;
    	if(t==0){
	    	try {
				ris = followersMng.follow(usr, text);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
    	}
    	else
    		ris = -2;
		return ris;
	}
	//end of subscription
    
    //logout function
    public void logout(){
    	connectTCP();
    	openStreams();
    	runner.terminate();
    	try {
			a.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
    	PayloadTCPFromClientToServer p = new PayloadTCPFromClientToServer(usr,psw,"logout",null,this.token,0,null);
    	try {
			out.writeObject(p);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	//Remove callback
    	try {
			followersMng.removeCallback(usr);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    	
    	closeStreams();
    	disconnectTCP();
    }//end of logout function

	//token checking locally
    private int localTokenCheck(String token2) {
    	String temp = token.substring(0,token.indexOf("-"));
		Long t = Long.parseLong(temp);
		if((t < System.currentTimeMillis()) || (token==null))
			return -1;
		else
			return 0;
 	}
    
    private void terminateThread(){
    	runner.terminate();
    	try {
			a.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
    }
    
}
