//The server main class that i need to execute to get the server up and running
package Server;
public class ServerMain {

	public static void main(String[] args){
		
		SocialServer s = new SocialServer();//Create new social server
		//initializing the server
		s.initializeServer();
		//setting up the keep alive thread for the server
		s.keepAliveThread();
		//Starting the RMI connection
		s.startRMI();
		//Starting TCP connection
		s.startTCP();
		
	}
}
