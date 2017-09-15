package Client;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class RemoteObjectClientImpl extends RemoteObject implements RemoteObjectClient{

	Gui g;
	private static final long serialVersionUID = 1L;

	public RemoteObjectClientImpl(Gui gui){
		this.g = gui;
	}

	@Override
	public void message(String myUsr, String message,String susr) throws RemoteException {
		
		g.addMessage(message);
		
	}
	

}
