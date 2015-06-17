package pt.ulisboa.tecnico.amorphous.requestrouter.internal;

import java.net.InetAddress;

public class RequestRouterCluster extends GenericNetworkService {

	public RequestRouterCluster(InetAddress serverIP, int serverPort) {
		super(serverIP, serverPort);
	}
}
