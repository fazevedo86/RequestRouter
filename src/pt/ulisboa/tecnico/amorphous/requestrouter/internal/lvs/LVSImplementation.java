package pt.ulisboa.tecnico.amorphous.requestrouter.internal.lvs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.tecnico.amorphous.requestrouter.internal.shell.SystemShell;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.types.Cluster;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.types.GenericNetworkService;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.types.Server;

public class LVSImplementation{
	private static final Logger logger = LoggerFactory.getLogger(LVSImplementation.class);
	
	private static String CMD_CLEAR_CLUSTERS = "ipvsadm -C";
	
	private static String CMD_SHOW_CLUSTERS = "ipvsadm -ln";
	private static String CMD_LIST_CONN = "ipvsadm -lcn --sort";
	private static String CMD_SHOW_CLUSTER_MEMBERS = "ipvsadm -ln -t CLUSTER_IP:CLUSTER_PORT";
	
	private static String CMD_ADD_CLUSTER = "ipvsadm -A -t CLUSTER_IP:CLUSTER_PORT -s lc";
	private static String CMD_REMOVE_CLUSTER = "ipvsadm -D -t CLUSTER_IP:CLUSTER_PORT";
	private static String CMD_ADD_SERVER = "ipvsadm -a -t CLUSTER_IP:CLUSTER_PORT -r SERVER_IP:SERVER_PORT -m -w 1";
	private static String CMD_REMOVE_SERVER = "ipvsadm -d -t CLUSTER_IP:CLUSTER_PORT -r SERVER_IP:SERVER_PORT";
	
	private static String REGEX_IP_PORT = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{3,5}$";
	private static String MARKER_CLUSTER_SERVER = "->";
	
	public LVSImplementation() {
	}
	
	private static List<String> executeCommand(String cmd){
		try {
			LVSImplementation.logger.info("Executing command: " + cmd);
			List<String> result = SystemShell.executeCommand(cmd);
			if(!result.isEmpty())
				for(String rline : result)
					LVSImplementation.logger.info(rline);
			else
				LVSImplementation.logger.info("(Execution produced no output)");
			
			return result;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static <T extends GenericNetworkService> T extractNetworkService(String line, Class<T> Type){
		String[] lineBits = line.split(" ");
		for(String bit : lineBits){
			if(bit.matches(LVSImplementation.REGEX_IP_PORT)){
					String[] address = bit.split(":");
					try {
						return Type.getConstructor(InetAddress.class,int.class).newInstance(InetAddress.getByName(address[0]), Integer.valueOf(address[1]));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		return null;
	}
	
	private static String prepareCmd(String cmd, Cluster cluster){
		return cmd.replaceFirst("CLUSTER_IP", cluster.getIP().getHostAddress()).replaceFirst("CLUSTER_PORT", String.valueOf(cluster.getPort()));
	}
	
	private static String prepareCmd(String cmd, Cluster cluster, Server server){
		return cmd.replaceFirst("CLUSTER_IP", cluster.getIP().getHostAddress()).replaceFirst("CLUSTER_PORT", String.valueOf(cluster.getPort())).replaceFirst("SERVER_IP", server.getIP().getHostAddress()).replaceFirst("SERVER_PORT", String.valueOf(server.getPort()));
	}
	
	public static boolean addCluster(Cluster cluster){
		List<String> result = LVSImplementation.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_ADD_CLUSTER, cluster));
		if( result == null || result.isEmpty() )
			return true;
		else
			return false;
	}
	
	public static boolean deleteCluster(Cluster cluster){
		List<String> result = LVSImplementation.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_REMOVE_CLUSTER, cluster));
		if( result == null || result.isEmpty() )
			return false;
		else
			return true;
	}
	
	public static boolean addServer(Cluster cluster, Server server){
		List<String> result = LVSImplementation.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_ADD_SERVER, cluster, server));
		if( result == null || result.isEmpty() )
			return true;
		else
			return false;
	}
	
	public static boolean deleteServer(Cluster cluster, Server server){
		List<String> result = LVSImplementation.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_REMOVE_SERVER, cluster, server));
		if( result == null || result.isEmpty() )
			return true;
		else
			return false;
	}
	
	public static boolean deleteAllClusters(){		
		// Delete all existing clusters 
		List<String> result = LVSImplementation.executeCommand(LVSImplementation.CMD_CLEAR_CLUSTERS);
		if( result == null || result.isEmpty() )
			return true;
		else
			return false;
	}
	
	public static List<Cluster> getClusters(){
		ArrayList<Cluster> Clusters = new ArrayList<Cluster>();
		
		// Get all existing clusters 
		List<String> cmdOutput = LVSImplementation.executeCommand(LVSImplementation.CMD_SHOW_CLUSTERS);
		for(String outputLine : cmdOutput){
			outputLine = outputLine.trim();
			if(!outputLine.startsWith(LVSImplementation.MARKER_CLUSTER_SERVER)){
				Cluster c = LVSImplementation.extractNetworkService(outputLine, Cluster.class);
				if(c != null)
					Clusters.add(c);
			}
		}
		
		for(Cluster c : Clusters){
			for(Server s : LVSImplementation.getClusterMembers(c))
				c.addClusterMember(s);
		}
		
		return Clusters;
	}
	
	public static List<Server> getClusterMembers(Cluster cluster){
		ArrayList<Server> servers = new ArrayList<Server>();
		try {
			List<String> cmdOutput = SystemShell.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_SHOW_CLUSTER_MEMBERS, cluster));
			for(String outputLine : cmdOutput){
				outputLine = outputLine.trim();
				if(outputLine.startsWith(LVSImplementation.MARKER_CLUSTER_SERVER)){
					Server s = LVSImplementation.extractNetworkService(outputLine, Server.class);
					if(s != null)
						servers.add(s);
				}
			}
		} catch (IOException | InterruptedException e) {
		}
		LVSImplementation.logger.info("Found " + servers.size() + " servers for cluster " + cluster.getIP().getHostAddress() + ":" + cluster.getPort());
		return servers;
	}

	public static List<Server> getClusterMemberStatistics(Cluster cluster){
		ArrayList<Server> servers = new ArrayList<Server>();
		try {
			List<String> cmdOutput = SystemShell.executeCommand(LVSImplementation.CMD_LIST_CONN);
			for(String outputLine : cmdOutput){
				outputLine = outputLine.trim();
				if(outputLine.startsWith(LVSImplementation.MARKER_CLUSTER_SERVER)){
					Server s = LVSImplementation.extractNetworkService(outputLine, Server.class);
					if(s != null)
						servers.add(s);
				}
			}
		} catch (IOException | InterruptedException e) {
		}
		return null;
	}
}
