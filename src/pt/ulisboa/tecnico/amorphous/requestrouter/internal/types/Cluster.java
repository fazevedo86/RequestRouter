package pt.ulisboa.tecnico.amorphous.requestrouter.internal.types;

import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Cluster extends GenericNetworkService {

	protected Set<Server> serverList;
	
	public Cluster(InetAddress ClusterVirtualIP, int ClusterPort) {
		super(ClusterVirtualIP, ClusterPort);
		this.serverList = new ConcurrentSkipListSet<Server>(); 
	}

	public boolean isClusterMember(Server server){
		return this.serverList.contains(server);
	}
	
	public boolean addClusterMember(Server server){
		return this.serverList.add(server);
	}
	
	public boolean removeClusterMember(Server server){
		return this.serverList.remove(server);
	}
	
	@Override
	public int hashCode(){
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Cluster)
			return super.equals(o);
		
		return false;
	}

}
