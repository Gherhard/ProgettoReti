package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObjectClient extends Remote{

	public void message(String myUsr,String message,String susr) throws RemoteException;
	
}

