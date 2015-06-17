package pt.ulisboa.tecnico.amorphous.requestrouter.lvs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.tecnico.amorphous.requestrouter.internal.AmorphousServer;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.GenericNetworkService;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.RequestRouterCluster;
import pt.ulisboa.tecnico.amorphous.requestrouter.utils.SystemShell;

public class LVSImplementation{
	private static final Logger logger = LoggerFactory.getLogger(LVSImplementation.class);
	
	private static String CMD_LIST_CONN = "ipvsadm -lcn --sort";
	private static String CMD_SHOW_CLUSTER = "ipvsadm -ln -t ";
	
	private static String CMD_ADD_CLUSTER = "ipvsadm -A -t CLUSTER_IP:CLUSTER_PORT -s lc";
	private static String CMD_REMOVE_CLUSTER = "ipvsadm -D -t CLUSTER_IP:CLUSTER_PORT";
	private static String CMD_ADD_SERVER = "ipvsadm -a -t CLUSTER_IP:CLUSTER_PORT -r SERVER_IP:SERVER_PORT -m -w 1";
	private static String CMD_REMOVE_SERVER = "ipvsadm -d -t 172.30.10.10:6653 -r 172.30.20.101:6653";
	
	private static String REGEX_IP_PORT = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{3,5}$";
	private static String MARKER_CLUSTER_SERVER = "->";
	
	public LVSImplementation() {
		// TODO Auto-generated constructor stub
	}
	
	private static List<String> executeCommand(String cmd){
		try {
			LVSImplementation.logger.info("executing command: " + cmd);
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
	
	private static <T extends GenericNetworkService> T extractAddress(String line, Class<T> Type){
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
	
	private static String prepareCmd(String cmd, RequestRouterCluster cluster){
		return cmd.replaceFirst("CLUSTER_IP", cluster.IPAddress.getHostAddress()).replaceFirst("CLUSTER_PORT", String.valueOf(cluster.Port));
	}
	
	private static String prepareCmd(String cmd, RequestRouterCluster cluster, AmorphousServer server){
		return cmd.replaceFirst("CLUSTER_IP", cluster.IPAddress.getHostAddress()).replaceFirst("CLUSTER_PORT", String.valueOf(cluster.Port)).replaceFirst("SERVER_IP", server.IPAddress.getHostAddress()).replaceFirst("SERVER_PORT", String.valueOf(server.Port));
	}
	
	public static boolean addCluster(RequestRouterCluster cluster){
		List<String> result = LVSImplementation.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_ADD_CLUSTER, cluster));
		if( result == null || result.isEmpty() )
			return false;
		else
			return true;
	}
	
	public static boolean deleteCluster(RequestRouterCluster cluster){
		List<String> result = LVSImplementation.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_REMOVE_CLUSTER, cluster));
		if( result == null || result.isEmpty() )
			return false;
		else
			return true;
	}
	
	public static boolean addServer(RequestRouterCluster cluster, AmorphousServer server){
		List<String> result = LVSImplementation.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_ADD_SERVER, cluster, server));
		if( result == null || result.isEmpty() )
			return false;
		else
			return true;
	}
	
	public static boolean deleteServer(RequestRouterCluster cluster, AmorphousServer server){
		List<String> result = LVSImplementation.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_REMOVE_SERVER, cluster, server));
		if( result == null || result.isEmpty() )
			return false;
		else
			return true;
	}
	
	public static List<RequestRouterCluster> getClusters(){
		// TODO implement it
		return null;
	}
	
	public static List<AmorphousServer> getClusterMembers(RequestRouterCluster cluster){
		ArrayList<AmorphousServer> servers = new ArrayList<AmorphousServer>();
		try {
			List<String> cmdOutput = SystemShell.executeCommand(LVSImplementation.prepareCmd(LVSImplementation.CMD_SHOW_CLUSTER, cluster));
			for(String outputLine : cmdOutput){
				outputLine = outputLine.trim();
				if(outputLine.startsWith(LVSImplementation.MARKER_CLUSTER_SERVER)){
					servers.add((AmorphousServer)LVSImplementation.extractAddress(outputLine, AmorphousServer.class));
				}
			}
		} catch (IOException | InterruptedException e) {
		}
		return null;
	}

	public static List<AmorphousServer> getClusterMemberStatistics(RequestRouterCluster cluster){
		ArrayList<AmorphousServer> servers = new ArrayList<AmorphousServer>();
		try {
			List<String> cmdOutput = SystemShell.executeCommand(LVSImplementation.CMD_LIST_CONN);
			for(String outputLine : cmdOutput){
				outputLine = outputLine.trim();
				if(outputLine.startsWith(LVSImplementation.MARKER_CLUSTER_SERVER)){
					servers.add((AmorphousServer)LVSImplementation.extractAddress(outputLine, AmorphousServer.class));
					// TODO add the connection counters to the info
				}
			}
		} catch (IOException | InterruptedException e) {
		}
		return null;
	}
}
