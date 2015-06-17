package pt.ulisboa.tecnico.amorphous.requestrouter;

import java.util.List;

import pt.ulisboa.tecnico.amorphous.requestrouter.config.ConfigHelper;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.AmorphousServer;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.RequestRouterCluster;
import pt.ulisboa.tecnico.amorphous.requestrouter.lvs.LVSImplementation;
import pt.ulisboa.tecnico.amorphous.requestrouter.shell.RequestRouterShell;

public class RequestRouter {
	
	private final RequestRouterShell shell;
	private final ConfigHelper config;

	public RequestRouter(ConfigHelper ch, boolean cleanInstance, boolean interactive) {
		this.shell = new RequestRouterShell(this);
		this.config = ch;
		
		// TODO use the configuration
		// TODO integrate with multicast announcements
		
		if(cleanInstance)
			this.cleanup();
		if(interactive)
			this.shell.startShell();
	}
	
	public boolean addCluster(RequestRouterCluster rrCluster){
		return LVSImplementation.addCluster(rrCluster);
	}
	
	public boolean deleteCluster(RequestRouterCluster rrCluster){
		return LVSImplementation.deleteCluster(rrCluster);
	}
	
	public boolean addServer(RequestRouterCluster rrCluster, AmorphousServer server){
		return LVSImplementation.addServer(rrCluster, server);
	}
	
	public boolean deleteServer(RequestRouterCluster rrCluster, AmorphousServer server){
		return LVSImplementation.deleteServer(rrCluster, server);
	}
	
	public List<AmorphousServer> getClusterMembers(RequestRouterCluster cluster){
		return LVSImplementation.getClusterMembers(cluster);
	}
	
	public void cleanup(){
		// TODO delete all clusters
	}

}
