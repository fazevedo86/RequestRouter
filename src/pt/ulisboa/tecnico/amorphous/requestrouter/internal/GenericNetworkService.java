package pt.ulisboa.tecnico.amorphous.requestrouter.internal;

import java.net.InetAddress;

public class GenericNetworkService {

	public final InetAddress IPAddress;
	public final int Port;
	
	public GenericNetworkService(InetAddress serverIP, int serverPort) {
		this.IPAddress = serverIP;
		this.Port = serverPort;
	}

}
