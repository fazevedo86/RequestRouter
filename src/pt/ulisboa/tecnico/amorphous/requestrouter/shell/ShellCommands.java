package pt.ulisboa.tecnico.amorphous.requestrouter.shell;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import pt.ulisboa.tecnico.amorphous.requestrouter.RequestRouter;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.AmorphousServer;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.RequestRouterCluster;
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
			this.context.addCluster(new RequestRouterCluster(InetAddress.getByName(IP), Integer.valueOf(Port)));
			return "Added cluster " + IP + ":" + Port;
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
    
	@Command(description="Delete a RequestRouter cluster")
    public String deletecluster(@Param(name="ClusterIP", description="The IP address for the RequestRouter cluster") String IP, @Param(name="ClusterPort", description="The TCP port for the RequestRouter cluster") String Port) {
    	try {
			this.context.deleteCluster(new RequestRouterCluster(InetAddress.getByName(IP), Integer.valueOf(Port)));
			return "Added cluster " + IP + ":" + Port;
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
    
	@Command(description="List servers configured for a RequestRouter cluster")
    public String listservers(@Param(name="ClusterIP", description="The IP address for the RequestRouter cluster") String IP, @Param(name="ClusterPort", description="The TCP port for the RequestRouter cluster") String Port) {
    	try {
			List<AmorphousServer> servers = this.context.getClusterMembers(new RequestRouterCluster(InetAddress.getByName(IP), Integer.valueOf(Port)));
			for(AmorphousServer server : servers){
				System.out.println("* " + server.IPAddress.getHostAddress() + ":" + server.Port);
			}
			return "";
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
	
    @Command(description="Add an Amorphous server to an existing cluster")
    public String addserver(@Param(name="ClusterIP", description="The IP address for the RequestRouter cluster") String ClusterIP, @Param(name="ClusterPort", description="The TCP port for the RequestRouter cluster") String ClusterPort, 
    		@Param(name="ServerIP", description="The IP address for the Amorphous server") String ServerIP, @Param(name="ServerPort", description="The TCP port for the Amorphous cluster") String ServerPort) {
    	try {
			this.context.addServer(new RequestRouterCluster(InetAddress.getByName(ClusterIP), Integer.valueOf(ClusterPort)), new AmorphousServer(InetAddress.getByName(ServerIP), Integer.valueOf(ServerPort)));
			return "Server " + ServerIP + ":" + ServerPort + "added to cluster " + ClusterIP + ":" + ClusterPort;
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
    
    @Command(description="Delete an Amorphous server from an existing cluster")
    public String removeserver(@Param(name="ClusterIP", description="The IP address for the RequestRouter cluster") String ClusterIP, @Param(name="ClusterPort", description="The TCP port for the RequestRouter cluster") String ClusterPort, 
    		@Param(name="ServerIP", description="The IP address for the Amorphous server") String ServerIP, @Param(name="ServerPort", description="The TCP port for the Amorphous cluster") String ServerPort) {
    	try {
			this.context.deleteServer(new RequestRouterCluster(InetAddress.getByName(ClusterIP), Integer.valueOf(ClusterPort)), new AmorphousServer(InetAddress.getByName(ServerIP), Integer.valueOf(ServerPort)));
			return "Server " + ServerIP + ":" + ServerPort + "deleted from cluster " + ClusterIP + ":" + ClusterPort;
		} catch (UnknownHostException | NumberFormatException e) {
			return "An error occurred: " + e.getStackTrace().toString();
		}
    }
    
}
