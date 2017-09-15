package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Client.RemoteObjectClient;

public interface RemoteObjectServer extends Remote{
	
	public static final String OBJECT_NAME = "FOLLOWER_MANAGER";
	
	public int follow(String myUser,String name) throws RemoteException;
	
	public int addCallback(String myUsr ,RemoteObjectClient c) throws RemoteException;
	
	public int removeCallback(String usr) throws RemoteException;

}

