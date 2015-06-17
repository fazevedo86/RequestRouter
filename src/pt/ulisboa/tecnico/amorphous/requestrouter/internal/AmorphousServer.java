package pt.ulisboa.tecnico.amorphous.requestrouter.internal;

import java.net.InetAddress;

public class AmorphousServer extends GenericNetworkService {

	public AmorphousServer(InetAddress serverIP, int serverPort) {
		super(serverIP, serverPort);
	}
}
