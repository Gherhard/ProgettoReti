package Const;

//Every port and name and number i need for my Client and Server 
public final class Constants {
	private Constants() {
     
	}
	public static final String KeepAliveServerAddress = "224.0.0.26";
	public static final int KeepAliveServerPort = 8888;
	public static final int serverWorkers = 20;
	public static final int ServerSocketTCP = 10008;
	public static final String backUpFile = "backup";
	public static final String friendHostname = "127.0.0.1";
	public static final String serverHostname = "127.0.0.1";
	public static final int KeepAliveClientUDPort = 8887;
	public static final int packetSize = 512;
	public static final int RMIport = 9005;
}
