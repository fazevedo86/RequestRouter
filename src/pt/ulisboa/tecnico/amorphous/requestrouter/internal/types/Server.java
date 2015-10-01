package pt.ulisboa.tecnico.amorphous.requestrouter.internal.types;

import java.net.InetAddress;

public class Server extends GenericNetworkService {

	public Server(InetAddress serverIP, int serverPort) {
		super(serverIP, serverPort);
	}

	@Override
	public int hashCode(){
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Server)
			return super.equals(o);
		
		return false;
	}
}
