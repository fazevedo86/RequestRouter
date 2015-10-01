/**
 * @author filipe.azevedo@tecnico.ulisboa.pt
 * Instituto Superior Tecnico - 2015
 */

package pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.ipv4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.AmorphousCluster;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.messages.IAmorphClusterMessage;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.messages.InvalidAmorphClusterMessageException;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.messages.JoinCluster;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.messages.LeaveCluster;

public class ClusterCommunicator extends Thread {
	
	class InboundMessage {
		public InetAddress origin;
		public IAmorphClusterMessage msg;
		
		public InboundMessage(InetAddress origin, IAmorphClusterMessage msg){
			this.origin = origin;
			this.msg = msg;
		}
	}
	
	protected static final Logger logger = LoggerFactory.getLogger(ClusterCommunicator.class);
	private static volatile ClusterCommunicator instance = null;

	public static final int DATAGRAM_MTU = 1472; // standard MTU of 1500 bytes - 28 bytes of UDP header overhead
	public static final String LOCAL_MCAST_GROUP = "224.0.0.1";
	public static final int MIN_PORT = 1025;
	public static final int MAX_PORT = 65534;

	protected final AmorphousCluster amorphousClusterIntegration;
	
	protected final McastInboundSocket inMcastSocket;
	protected final McastOutboundSocket outMcastSocket;
	protected final InboundSocket inSocket;
	protected final InetAddress localmcastGroup;
	protected final InetAddress mcastGroup;
	protected final int clusterPort;
	protected volatile Queue<InboundMessage> inboundMsgQueue;
	
	
	public static ClusterCommunicator getInstance(){
		return ClusterCommunicator.instance;
	}
	
	public ClusterCommunicator() throws InstantiationException {
		throw new InstantiationException("An error occurred while creating an instance of " + ClusterCommunicator.class.toString() + ": Please use a constructor with an apropriate amount of arguments.");
	}
	
	public ClusterCommunicator(AmorphousCluster amorphousClusterIntegration, String mcastGroupIP, int Port) throws UnknownHostException, InstantiationException {
		synchronized(ClusterCommunicator.class){
			if(ClusterCommunicator.instance == null){
				if(Port < ClusterCommunicator.MIN_PORT || Port > ClusterCommunicator.MAX_PORT){
					throw new UnknownHostException("Invalid port was specified for multicast group " + mcastGroupIP + ": " + Port);
				}
				this.clusterPort = Port;
				this.mcastGroup = InetAddress.getByName(mcastGroupIP);
				this.localmcastGroup = InetAddress.getByName(ClusterCommunicator.LOCAL_MCAST_GROUP);
				
				ClusterCommunicator.instance = this;
			} else {
				throw new InstantiationException("An error occurred while creating an instance of " + ClusterCommunicator.class.toString() + ": An instance already exists.");
			}
		}
		
		this.amorphousClusterIntegration = amorphousClusterIntegration;
		
		this.inMcastSocket = new McastInboundSocket();
		this.outMcastSocket = new McastOutboundSocket();
		this.inSocket = new InboundSocket(this.getClusterPort());
		
		this.inboundMsgQueue = new ConcurrentLinkedQueue<InboundMessage>();
	}
	
	/**
	 * Start listening for incoming messages and sending out messages
	 * @return
	 */
	public boolean initCommunications() {
		// Boot the multicast group listner
		this.inMcastSocket.startSocket();
		if( this.inMcastSocket.startSocket() && this.outMcastSocket.startSocket() && this.inSocket.startSocket() ){
			this.inMcastSocket.start();
			this.outMcastSocket.start();
			this.inSocket.start();
			this.start();
			return true;
		}
		return false;
	}
	
	public boolean stopCommunications() {
		// Boot the multicast group listner
		return this.inMcastSocket.stopSocket() && this.outMcastSocket.stopSocket() && this.inSocket.stopSocket();
	}
	
	/**
	 * Determine if communications are still active.
	 * Communications are active if a socket is still active or if there are still
	 * inbound messages left to be processed
	 * @return
	 */
	public boolean isCommunicationActive(){
		return this.inMcastSocket.isActive() || this.outMcastSocket.isActive() || this.inSocket.isActive() || !this.inboundMsgQueue.isEmpty();
	}
	
	public int getClusterPort(){
		return this.clusterPort;
	}
	
	public InetAddress getGlobalMulticastGroup(){
		return this.mcastGroup;
	}
	
	public InetAddress getLocalMulticastGroup(){
		return this.localmcastGroup;
	}
	
	public void registerInboundMessage(InetAddress originNodeAddress, byte[] payload){
		IAmorphClusterMessage inMsg = null;

		try {
			inMsg = MessageCodec.getDecodedMessage(payload);
		} catch (InvalidAmorphClusterMessageException e) {
			ClusterCommunicator.logger.error(e.getMessage());
			return;
		}

		if( (inMsg != null) && ((inMsg instanceof JoinCluster) || (inMsg instanceof LeaveCluster)) )
			this.inboundMsgQueue.add(new InboundMessage(originNodeAddress, inMsg));
	}
	
	/**
	 * Start dispatching received messages
	 */
	@Override
	public void run() {
		
		while(this.isCommunicationActive()){
			// Wait for it...
			while(this.inboundMsgQueue.isEmpty()) {
				try {
					sleep(500);
				} catch (InterruptedException e) {
					ClusterCommunicator.logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
			// Process all queued inbound messages
			while(!this.inboundMsgQueue.isEmpty()){
				InboundMessage inmsg = this.inboundMsgQueue.poll();
				this.amorphousClusterIntegration.processClusterMessage(inmsg.origin, inmsg.msg);
			}
		}
	}

}
