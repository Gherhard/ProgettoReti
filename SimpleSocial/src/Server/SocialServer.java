package Server;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static Const.Constants.serverWorkers;
import static Const.Constants.ServerSocketTCP;
import static Const.Constants.backUpFile;
import static Const.Constants.RMIport;

public class SocialServer {

	public static ConcurrentHashMap<String,User> registeredUsers = new ConcurrentHashMap<String,User>();
    public static ConcurrentHashMap<String,User> onlineUsers = new ConcurrentHashMap<String,User>();
    KeepAliveServerThread kas = null;//KeepAlive
    RemoteObjectServer a = null;//RMI
    ServerSocket serverSocket = null;//server socket for TCP connection
    
	
	@SuppressWarnings("unchecked")
	public void initializeServer() {
		
		System.setProperty("java.net.preferIPv4Stack" , "true");
		
		//read from backup file
		File toRead = new File(backUpFile);
		//creating temporary hashmap for registered users
		ConcurrentHashMap<String,User> ris = new ConcurrentHashMap<String,User>();
		try {
			FileInputStream f = new FileInputStream(toRead);
			ObjectInputStream in = new ObjectInputStream(f);
			
			ris = (ConcurrentHashMap<String,User>) in.readObject();
			
			in.close();
			f.close();
			for(String a: ris.keySet() ){
				String v = ris.get(a).getUsername();
				System.out.println("Imported "+v);
			}
			//for testing only
			/*for(User t: ris.values()){
				System.out.println("User "+t.getUsername());
				System.out.println("Friends "+t.getFriendList());
				System.out.println("Followers "+t.getFollowers());
			}*/
		} catch (NullPointerException e){
			System.err.println("No friends loaded!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			System.err.println("backUp file is empty!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//got my registered users map loaded from the backup file
		registeredUsers.putAll(ris);
	}

	public void keepAliveThread() {
		//executing the keepAliveServerThread
		//there is one keepalive thread for the server. And every client communicates with this thread with it's own keepalive thread
		ExecutorService keepalive = Executors.newSingleThreadExecutor();
		kas = new KeepAliveServerThread();
		keepalive.submit(kas);
		
	}
	//starting the RMI service
	public void startRMI() {
		try {
			LocateRegistry.createRegistry(RMIport);
			a = (RemoteObjectServer) UnicastRemoteObject.exportObject(new RemoteObjectServerImpl(),0);
			Registry registry = LocateRegistry.getRegistry(RMIport);
	        registry.rebind(RemoteObjectServer.OBJECT_NAME, a);
	        System.out.println("Followers manager is Ready");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
         
		
	}

	//creating the TCP connection and the threads for every client connected to the server through TCP
	public void startTCP() {
		ExecutorService es= Executors.newFixedThreadPool(serverWorkers);//thread pool
		try {
	        serverSocket = new ServerSocket(ServerSocketTCP); 
	        //System.out.println ("Connection Socket Created");
	        
            while (true)
            {
                 System.out.println ("Waiting for Connection");
                 Socket client = serverSocket.accept();
                 System.out.println ("Client arrived");
                 ServerWorker handler = new ServerWorker(client);//create new worker and send him the socket
                 es.submit(handler);//new thread for new client
            }//closes while
		 } catch (RemoteException e) {
			 System.out.println("Server Followers Manager error "+e.getMessage());
			 System.exit(1);
         } catch (IOException e) {
        	 System.err.println("Error: ServerIOException");
       	  	 e.printStackTrace();
             System.exit(1);  
         } finally {
                 es.shutdown();
         }
		
	}
	
}
