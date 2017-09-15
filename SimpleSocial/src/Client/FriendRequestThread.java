package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


/*This is the friendRequestThread class, and manages the friends of a client. Which means that every time the client gets a friend request,
 *  this thread creates a TCP connection between client and it's new friend.
 *  Everything is done with a secondary TCP connection and a friend port which every user has. */
public class FriendRequestThread implements Runnable{
	
	ServerSocket friendSocket;
	private int friendPort;

	public FriendRequestThread(ServerSocket friendSocket, int friendPort) {
		this.friendSocket = friendSocket;
		this.friendPort = friendPort;
	}

	
	public void run() {
		
		String message = "";
		while(true){
			System.out.println ("Friend Request thread waiting for connection on port " +friendPort);
			try {
				Socket friend = friendSocket.accept();
				System.out.println("Received request!");
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(friend.getInputStream()));
				message = reader.readLine();
				System.out.println("Request received from "+message);
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
}