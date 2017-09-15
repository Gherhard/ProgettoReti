package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import static Const.Constants.KeepAliveClientUDPort;
import static Const.Constants.KeepAliveServerAddress;
import static Const.Constants.KeepAliveServerPort;
import static Const.Constants.packetSize;

public class KeepAliveClientThread implements Runnable{
	
	String user;
	private volatile boolean running = true;
	private boolean sendPacket = true;
	
	public KeepAliveClientThread(String usr) {
		this.user = usr;
	}

	public void terminate(){
		running = false;
		sendPacket = false;
	}
	//this thread gets interrupted and waited for before the interruption, in the Server
	//it terminates when the client goes offline(Logout)
	//if the keepalive thread does not work for some reason, the client won't answer and it will be logged out.
	@Override
	public void run() {
		try {
			System.out.println("Hey i'm "+user+", this is my keepalive thread!");
			MulticastSocket client = new MulticastSocket(KeepAliveServerPort);
			InetAddress multicastGroup=InetAddress.getByName(KeepAliveServerAddress);
			//joining the multicast group so it can receive and send the keep alive UDP packet
	        client.joinGroup(multicastGroup);
	        byte[] b = new byte[packetSize];
			DatagramPacket receivepacket= new DatagramPacket(b,b.length);
			while(running){
				client.receive(receivepacket);
				b = receivepacket.getData();
				if(sendPacket)
					sendUDPacket();
			}
			client.leaveGroup(multicastGroup);
			client.close();
			//System.out.println("I left the multicast group!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	//sending UDP packet to the keep alive server thread.
	private void sendUDPacket() {
		try {
			DatagramSocket serverSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");
			//create packet
        	byte [] data = user.getBytes();
        	DatagramPacket sendudpPacket = new DatagramPacket(data, data.length, IPAddress , KeepAliveClientUDPort);//8887
        	//send packet
        	serverSocket.send(sendudpPacket);
        	//System.out.println("KeepAliveClientThread : I've sent the DatagramPacket!");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}


