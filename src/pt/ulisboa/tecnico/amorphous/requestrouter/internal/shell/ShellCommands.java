package pt.ulisboa.tecnico.amorphous.requestrouter.internal.shell;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import pt.ulisboa.tecnico.amorphous.requestrouter.RequestRouter;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.types.Cluster;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.types.Server;
import asg.cliche.Command;
import asg.cliche.Param;

public class ShellCommands {

	private final RequestRouter context;
	
	public ShellCommands(RequestRouter context) {
		this.context = context;
	}
	
	@Command
	public String help(){
		return "please use \"?help\" instead";
	}
	
	@Command
    public void quit() {
		this.context.cleanup();
        System.exit(0);
    }

	@Command(description="Add a new RequestRouter cluster")
    public String addcluster(@Param(name="ClusterIP", description="The IP address for the RequestRouter cluster") String IP, @Param(name="ClusterPort", description="The TCP port for the RequestRouter cluster") String Port) {
    	try {
			boolean result = this.context.addCluster(new Cluster(InetAddress.getByName(IP), Integer.valueOf(Port)));
			if(result)
				return "Added cluster " + IP + ":" + Port;
			else
				return "Failed to add new cluster";
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
    
	@Command(description="Delete a RequestRouter cluster")
    public String deletecluster(@Param(name="ClusterIP", description="The IP address for the RequestRouter cluster") String IP, @Param(name="ClusterPort", description="The TCP port for the RequestRouter cluster") String Port) {
    	try {
			boolean result = this.context.deleteCluster(new Cluster(InetAddress.getByName(IP), Integer.valueOf(Port)));
			if(result)
				return "Added cluster " + IP + ":" + Port;
			else
				return "Failed to remove the cluster";
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
    
	@Command(description="List servers configured for a RequestRouter cluster")
    public String listservers(@Param(name="ClusterIP", description="The IP address for the RequestRouter cluster") String IP, @Param(name="ClusterPort", description="The TCP port for the RequestRouter cluster") String Port) {
    	try {
			List<Server> servers = this.context.getClusterMembers(new Cluster(InetAddress.getByName(IP), Integer.valueOf(Port)));
			if(servers != null){
				System.out.println("Active servers for cluster" + IP + ":" + Port);
				for(Server server : servers){
					System.out.println("* " + server.getIP().getHostAddress() + ":" + server.getPort());
				}
				return "";
			} else {
				return "No servers found for this cluster";
			}
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
	
    @Command(description="Add an Amorphous server to an existing cluster")
    public String addserver(@Param(name="ClusterIP", description="The IP address for the RequestRouter cluster") String ClusterIP, @Param(name="ClusterPort", description="The TCP port for the RequestRouter cluster") String ClusterPort, 
    		@Param(name="ServerIP", description="The IP address for the Amorphous server") String ServerIP, @Param(name="ServerPort", description="The TCP port for the Amorphous cluster") String ServerPort) {
    	try {
			boolean result = this.context.addServer(new Cluster(InetAddress.getByName(ClusterIP), Integer.valueOf(ClusterPort)), new Server(InetAddress.getByName(ServerIP), Integer.valueOf(ServerPort)));
			if(result)
				return "Server " + ServerIP + ":" + ServerPort + "added to cluster " + ClusterIP + ":" + ClusterPort;
			else
				return "Failed to add the server to the cluster";
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
    
    @Command(description="Delete an Amorphous server from an existing cluster")
    public String removeserver(@Param(name="ClusterIP", description="The IP address for the RequestRouter cluster") String ClusterIP, @Param(name="ClusterPort", description="The TCP port for the RequestRouter cluster") String ClusterPort, 
    		@Param(name="ServerIP", description="The IP address for the Amorphous server") String ServerIP, @Param(name="ServerPort", description="The TCP port for the Amorphous cluster") String ServerPort) {
    	try {
			boolean result = this.context.deleteServer(new Cluster(InetAddress.getByName(ClusterIP), Integer.valueOf(ClusterPort)), new Server(InetAddress.getByName(ServerIP), Integer.valueOf(ServerPort)));
			if(result)
				return "Server " + ServerIP + ":" + ServerPort + "deleted from cluster " + ClusterIP + ":" + ClusterPort;
			else
				return "Failed to remove the server from the cluster";
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
    
}
