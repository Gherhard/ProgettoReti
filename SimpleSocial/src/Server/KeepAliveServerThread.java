	package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static Const.Constants.KeepAliveServerAddress;
import static Const.Constants.KeepAliveServerPort;
import static Const.Constants.KeepAliveClientUDPort;
import static Const.Constants.packetSize;
public class KeepAliveServerThread implements Runnable{

	int timetosend = 1;
	//This thread creates the multicast socket.
	//Sets a timeout and sends a message to everyone on the multicast socket every 10 sec
	public void run() {
		MulticastSocket server = null;
		DatagramSocket udpclient = null;
		int timeout = 10000;
        Long startime = 0L;
        
		try {
			server = new MulticastSocket(KeepAliveServerPort);
			server.setTimeToLive(1);
			server.setLoopbackMode(false);
			server.setReuseAddress(true);
			udpclient = new DatagramSocket(KeepAliveClientUDPort);
			udpclient.setSoTimeout(timeout);
			udpclient.setReuseAddress(true);
			InetAddress multicastGroup=InetAddress.getByName(KeepAliveServerAddress);
			String t = "keepalive";
			
			byte[] senddata = t.getBytes();
			DatagramPacket spacket = new DatagramPacket(senddata,senddata.length,multicastGroup,KeepAliveServerPort);
	        
	        while(true){
		        	if(timetosend == 1){
		        		timeout = 10000;
		        		server.send(spacket);
			        	startime = System.currentTimeMillis();
		        		timetosend = 0;//goes back to 1 only when the timeout expired in the receiveUDPacket below.
		        	}
		        	receiveUDPacket(timeout,startime,udpclient);
	        }
			
		} catch (IOException e) {
			System.out.println("KeepAliveServerThread: Some error appeared: "+e.getMessage());
			server.close();
		}
		
		
	}

	private void receiveUDPacket(int timeout, Long startime,DatagramSocket udpclient) {
		try {
			byte[] receivedata = new byte[packetSize];
			DatagramPacket receivepacket= new DatagramPacket(receivedata,receivedata.length);
        	udpclient.receive(receivepacket);
        	receivedata = receivepacket.getData();
        	String textReceived = new String(receivedata,0,receivepacket.getLength());
        	SocialServer.onlineUsers.get(textReceived).setFeedback(true);
        	timeout = (int) (timeout - (System.currentTimeMillis() - startime));
        	if(timeout<=0)
        		timetosend = 1;
        	else
        		timetosend = 0;
        		
		}catch(SocketTimeoutException e){
        	timetosend = 1;
        	checkonline();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//This function checks to see who answered the keepalive message and it counts them.
	//By using the feedback attribute that every user has and that is set to TRUE only when they answer the keep alive message
	private void checkonline() {

		HashMap<String,User> map = new HashMap<String,User> (SocialServer.onlineUsers);
		for (Iterator<Map.Entry<String, User>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, User> entry = iterator.next();
				User user = entry.getValue();
				if (!user.getFeedback()) {
					iterator.remove();
					SocialServer.registeredUsers.get(user.getUsername()).setToken(null);//this way at his next operation he goes to the login window
				} else{
					user.setFeedback(false);
				}
		}
		System.out.println("KeepAliveServerThread: Online Users: "+ map.size());
	}

}

