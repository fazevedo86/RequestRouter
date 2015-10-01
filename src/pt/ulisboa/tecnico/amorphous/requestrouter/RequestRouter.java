package pt.ulisboa.tecnico.amorphous.requestrouter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.AmorphousCluster;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.types.ClusterNode;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.config.ConfigHelper;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.config.ConfigOptionsHelper;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.lvs.LVSImplementation;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.shell.RequestRouterShell;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.types.Cluster;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.types.Server;

public class RequestRouter {
	private static final Logger logger = LoggerFactory.getLogger(RequestRouter.class);
	
	private final RequestRouterShell shell;
	private final ConfigHelper config;
	private final Cluster amorphousVirtualCluster;
	private final AmorphousCluster amorphousClusterIntegration;

	public RequestRouter(ConfigOptionsHelper coh, boolean cleanInstance, boolean interactive) throws NumberFormatException, UnknownHostException, InstantiationException {
		this.shell = new RequestRouterShell(this);
		this.config = new ConfigHelper(coh);
		
		Map<String, String> configs = coh.getAllConfigs();
		
		this.amorphousVirtualCluster = new Cluster( InetAddress.getByName(configs.get(ConfigOptionsHelper.KEY_RRCLUSTER_IP)), Integer.parseInt(configs.get(ConfigOptionsHelper.KEY_RRCLUSTER_PORT)));
		this.amorphousClusterIntegration = new AmorphousCluster(this, configs.get(ConfigOptionsHelper.KEY_AMORPH_GROUP), Integer.parseInt(configs.get(ConfigOptionsHelper.KEY_AMORPH_PORT)), Integer.parseInt(configs.get(ConfigOptionsHelper.KEY_AMORPH_HELLO_INTERVAL)));
		
		if(cleanInstance){
			this.cleanup();
		} else {
			// Import existing servers
			List<Server> configuredServers = this.getClusterMembers(this.amorphousVirtualCluster);
			List<ClusterNode> clusterNodes = new ArrayList<ClusterNode>(configuredServers.size());
			for(Server s : configuredServers){
				RequestRouter.logger.debug("Importing a server from existing cluster config...");
				clusterNodes.add(new ClusterNode(s.getIP(), String.valueOf(s.getPort())));
			}
			this.amorphousClusterIntegration.importState(clusterNodes);
		}
		
		LVSImplementation.addCluster(this.amorphousVirtualCluster);

			
		if(interactive)
			this.shell.startShell();
		
		this.amorphousClusterIntegration.startClusterService();
	}
	
	public boolean addCluster(Cluster rrCluster){
		return LVSImplementation.addCluster(rrCluster);
	}
	
	public boolean deleteCluster(Cluster rrCluster){
		return LVSImplementation.deleteCluster(rrCluster);
	}
	
	public boolean addServer(Cluster rrCluster, Server server){
		return LVSImplementation.addServer(rrCluster, server);
	}
	
	public boolean deleteServer(Cluster rrCluster, Server server){
		return LVSImplementation.deleteServer(rrCluster, server);
	}
	
	public List<Server> getClusterMembers(Cluster cluster){
		return LVSImplementation.getClusterMembers(cluster);
	}
	
	public List<Cluster> getAllClusters(){
		return LVSImplementation.getClusters();
	}
	
	public boolean addServer(ClusterNode amorphousNode){
		Server server = new Server(amorphousNode.getNodeIP(), this.amorphousVirtualCluster.getPort());
		return this.addServer(this.amorphousVirtualCluster, server);
	}
	
	public boolean deleteServer(ClusterNode amorphousNode){
		Server server = new Server(amorphousNode.getNodeIP(), this.amorphousVirtualCluster.getPort());
		return this.deleteServer(this.amorphousVirtualCluster, server);	}
	
	public void cleanup(){
		LVSImplementation.deleteAllClusters();
	}

}
