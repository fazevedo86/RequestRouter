package pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.tecnico.amorphous.requestrouter.RequestRouter;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.ipv4.ClusterCommunicator;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.messages.IAmorphClusterMessage;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.messages.JoinCluster;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.types.ClusterNode;


public class AmorphousCluster extends Thread{
	protected static final Logger logger = LoggerFactory.getLogger(AmorphousCluster.class);

	private static final Long MAX_FAILED_HELLO = 3L;
	
	protected final Integer helloInterval;
	protected final ClusterCommunicator clusterComm;
	protected final RequestRouter requestRouter;
	
	protected ConcurrentMap<InetAddress,ClusterNode> nodes;
	protected volatile Long latestHello;
	protected volatile boolean isRunning; 

	
	public AmorphousCluster(RequestRouter rr, String mcastGroupIP, Integer Port, Integer HelloInterval) throws UnknownHostException, InstantiationException {
		this.clusterComm = new ClusterCommunicator(this, mcastGroupIP, Port);
		
		// Initialize the cluster node set
		this.nodes = new ConcurrentHashMap<InetAddress,ClusterNode>();
		
		this.requestRouter = rr;
		this.helloInterval = HelloInterval;
		this.isRunning = false;
	}
	
	
	
	//------------------------------------------------------------------------
	//						Cluster Integration Service
	//------------------------------------------------------------------------

	public boolean startClusterService() {
		if(!this.isClusterServiceRunning()){
			this.clusterComm.initCommunications();
			this.isRunning = true;
			this.start();
		}
		return this.isClusterServiceRunning();
	}

	public boolean stopClusterService() {
		if(this.isClusterServiceRunning()){
			this.isRunning = false;
			return this.clusterComm.stopCommunications();
		}
		return false;
	}
	
	public boolean isClusterServiceRunning(){
		return this.clusterComm.isCommunicationActive();
	}

	//------------------------------------------------------------------------


	//------------------------------------------------------------------------
	//						Cluster Node Management
	//------------------------------------------------------------------------

	public void importState(List<ClusterNode> servers){
		for(ClusterNode node : servers){
			this.nodes.put(node.getNodeIP(), node);
		}
		
		this.printClusterStatus();
	}
	
	public ClusterNode getClusterNode(InetAddress ip){
		return this.nodes.get(ip);
	}
	
	public ClusterNode getClusterNode(String NodeID){
		for(ClusterNode node : this.nodes.values())
			if(node.getNodeID().equals(NodeID))
				return node;
		
		return null;
	}
	
	public boolean isClusterNode(ClusterNode node) {
		return this.nodes.containsKey(node.getNodeIP());
	}

	public Collection<ClusterNode> getClusterNodes() {
		return Collections.unmodifiableCollection(this.nodes.values());
	}
	
	private boolean addClusterNode(ClusterNode node) {
		if(this.isClusterNode(node)){
			ClusterNode existingNode = this.nodes.get(node.getNodeIP());
			if(!existingNode.getNodeID().equals(node.getNodeID())){
				// Different node ID from a previously registered node implies new execution of the controller on said node
				this.removeClusterNode(existingNode);
				this.nodes.put(node.getNodeIP(), node);
				
				AmorphousCluster.logger.debug("Node " + node.getNodeID() + "(" + node.getNodeIP().getHostAddress() + ") added!");
				this.printClusterStatus();
				
				// Integrate with Request Router
				if(!this.requestRouter.addServer(node)){
					this.nodes.remove(node.getNodeIP());
					AmorphousCluster.logger.debug("Attempt to add node " + node.getNodeID() + "(" + node.getNodeIP() + ") failed!");
					return false;
				}
				
				return true;
			} else {
				// Update node's last seen timestamp
				existingNode.refresh();
			}
		} else {
			this.nodes.put(node.getNodeIP(), node);
			
			AmorphousCluster.logger.debug("Node " + node.getNodeID() + "(" + node.getNodeIP().getHostAddress() + ") added!");
			this.printClusterStatus();
			
			return true;
		}
			
		return false;
	}
	
	private void removeClusterNode(ClusterNode node) {
		ClusterNode removedNode = this.nodes.remove(node.getNodeIP());
		if(removedNode == null){
			AmorphousCluster.logger.debug("Attempted to remove unregistered node " + node.getNodeID() + "(" + node.getNodeIP() + ")");
		} else {
			AmorphousCluster.logger.debug("Node " + removedNode.getNodeID() + "(" + removedNode.getNodeIP() + ") removed!");
			
			// Integrate with Request Router
			if(!this.requestRouter.deleteServer(node)){
				this.nodes.put(node.getNodeIP(), node);
				AmorphousCluster.logger.debug("Attempt to remove node " + node.getNodeID() + "(" + node.getNodeIP() + ") failed!");
			}
			
			this.printClusterStatus();
		}
	}
	
	public void printClusterStatus(){
		StringBuilder members = new StringBuilder("\n[AMORPHOUS] Cluster membership:");
		for(ClusterNode node : this.nodes.values()){
			members.append("\n" +  node.getNodeIP().getHostName() + " sessionId=" + node.getNodeID());
		}
		members.append("\n");
		System.out.println(members);
	}
	
	public void processClusterMessage(InetAddress NodeAddress, IAmorphClusterMessage msg) {
		AmorphousCluster.logger.debug("Processing message from node " + msg.getOriginatingNodeId() + "(" + NodeAddress.getHostAddress() + ")");

		// Dispatch message handling to accordingly method
		try {
			AmorphousCluster.class.getDeclaredMethod("handleMessage" + msg.getMessageType().getSimpleName(), InetAddress.class, IAmorphClusterMessage.class).invoke(this, NodeAddress, msg);
		} catch(NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e){
			AmorphousCluster.logger.error("(" + e.getClass().getSimpleName() + ") Unable to find fitting method: " + e.getMessage());
		}
		
	}

	@SuppressWarnings("unused")
	private void handleMessageJoinCluster(InetAddress origin, IAmorphClusterMessage message){
		JoinCluster msg = (JoinCluster)message;
		ClusterNode neighbor = new ClusterNode(origin, message.getOriginatingNodeId());
			
		AmorphousCluster.logger.debug("Processing JoinCluster message from node " + message.getOriginatingNodeId() + "(" + origin.getHostAddress() + ")");
		if( this.addClusterNode(new ClusterNode(origin, message.getOriginatingNodeId())) ){
			// TODO: Propagate event to the Request Router
		}
	}

	@SuppressWarnings("unused")
	private void handleMessageLeaveCluster(InetAddress origin, IAmorphClusterMessage message){
		this.removeClusterNode(new ClusterNode(origin, message.getOriginatingNodeId()));
	}

	@Override
	public void run() {
		while(this.isRunning || this.isClusterServiceRunning()){
			Long currTime = System.currentTimeMillis();
			if( (currTime - this.latestHello) >= this.helloInterval ){
				for(ClusterNode node : this.nodes.values()){
					if( node.getNodeAge() >= (this.helloInterval * AmorphousCluster.MAX_FAILED_HELLO) ){
						this.removeClusterNode(node);
					}
				}
			}
			
			// Sleep it out
			try {
				sleep(this.helloInterval);
			} catch (InterruptedException e) {
				AmorphousCluster.logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
	}
}
