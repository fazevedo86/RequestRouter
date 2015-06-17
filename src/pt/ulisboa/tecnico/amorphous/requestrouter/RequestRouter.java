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
	
	public void addCluster(RequestRouterCluster rrCluster){
		LVSImplementation.addCluster(rrCluster);
	}
	
	public void deleteCluster(RequestRouterCluster rrCluster){
		LVSImplementation.deleteCluster(rrCluster);
	}
	
	public void addServer(RequestRouterCluster rrCluster, AmorphousServer server){
		LVSImplementation.addServer(rrCluster, server);
	}
	
	public void deleteServer(RequestRouterCluster rrCluster, AmorphousServer server){
		LVSImplementation.deleteServer(rrCluster, server);
	}
	
	public List<AmorphousServer> getClusterMembers(RequestRouterCluster cluster){
		return LVSImplementation.getClusterMembers(cluster);
	}
	
	public void cleanup(){
		// TODO delete all clusters
	}

}
