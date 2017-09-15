package Server;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import Client.RemoteObjectClient;


public class RemoteObjectServerImpl extends RemoteObject implements RemoteObjectServer{

	private static final long serialVersionUID = 1L;
	
	@Override
	public int addCallback(String myUsr ,RemoteObjectClient c) throws RemoteException {
		if(SocialServer.registeredUsers.containsKey(myUsr)){
			SocialServer.registeredUsers.get(myUsr).setMyRmi(c);//register callback
			System.out.println("Callback Added");
			return 0;
		}
		else {
			System.out.println("Callback Not Added");
			return -1;
		}
			
	}

	@Override
	public int follow(String myUsr,String usr) throws RemoteException {
			if(SocialServer.registeredUsers.get(myUsr).getFriendList().contains(usr)){
				SocialServer.registeredUsers.get(usr).getFollowers().add(myUsr);
				return 0;
			}
			else 
				return -1;
	}

	@Override
	public int removeCallback(String usr) throws RemoteException {
		if(SocialServer.registeredUsers.containsKey(usr)){
			SocialServer.registeredUsers.get(usr).setMyRmi(null);//register callback
			System.out.println("Callback Added");
			return 0;
		}
		else {
			System.out.println("Callback Not Added");
			return -1;
		}
	}


}

