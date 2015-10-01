package pt.ulisboa.tecnico.amorphous.requestrouter.internal.types;

import java.net.InetAddress;

public class GenericNetworkService {

	public final InetAddress serviceIPAddress;
	public final int servicePort;

	public GenericNetworkService(InetAddress serverIP, int serverPort) {
		this.serviceIPAddress = serverIP;
		this.servicePort = serverPort;
	}

	public InetAddress getIP(){
		return this.serviceIPAddress;
	}
	
	public int getPort(){
		return this.servicePort;
	}
	
	@Override
	public int hashCode(){
		return (this.serviceIPAddress.hashCode() + this.servicePort) / this.servicePort;
	}
    	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof GenericNetworkService))
			return false;
		
		GenericNetworkService target = ((GenericNetworkService)o);
		return this.serviceIPAddress.equals(target.serviceIPAddress) && (this.servicePort == target.servicePort);
	}
}