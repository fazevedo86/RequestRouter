package pt.ulisboa.tecnico.amorphous.requestrouter.internal.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.args4j.Option;

public class ConfigOptionsHelper {
    private static final String DEFAULT_CONFIG_FILE = "RequestRouter.cfg";
    
    public static final String KEY_RRCLUSTER_IP = "RequestRouterIP";
    private static final String DEFAULT_RRCLUSTER_IP = "172.30.20.10";
    
    public static final String KEY_RRCLUSTER_PORT = "RequestRouterPort";
    private static final String DEFAULT_RRCLUSTER_PORT = "6653";
    
    public static final String KEY_AMORPH_GROUP = "AmorphGroup";
    private static final String DEFAULT_AMORPH_GROUP = "224.0.1.20";
    
    public static final String KEY_AMORPH_PORT = "AmorphPort";
    private static final String DEFAULT_AMORPH_PORT = "6969";

    public static final String KEY_AMORPH_HELLO_INTERVAL = "AmorphHelloInterval";
    private static final String DEFAULT_AMORPH_HELLO_INTERVAL = "15000";
    
    @Option(name="-cf", aliases="--configFile", metaVar="FILE", usage="RequestRouter configuration file")
    private String configFile = DEFAULT_CONFIG_FILE;
    @Option(name="-rrip", aliases="--RequestRouterIP", metaVar="IPv4", usage="The IP address for the RequestRouter cluster")
    private String rrClusterIP = DEFAULT_RRCLUSTER_IP;
    @Option(name="-rrport", aliases="--RequestRouterPort", metaVar="int", usage="The TCP Port for the RequestRouter cluster")
    private String rrClusterPort = DEFAULT_RRCLUSTER_PORT;
    @Option(name="-agrp", aliases="--AmorphGroup", metaVar="IPv4", usage="The Amorphous cluster IPv4 Multicast address")
    private String amorphGroup = DEFAULT_AMORPH_GROUP;
    @Option(name="-aprt", aliases="--AmorphPort", metaVar="int", usage="The TCP Port for the Amorphous cluster")
    private String amorphClusterPort = DEFAULT_AMORPH_PORT;
    @Option(name="-aint", aliases="--AmorphHelloInterval", metaVar="int", usage="The Hello Interval to be considered for the Amorphous cluster")
    private String amorphClusterHelloInterval = DEFAULT_AMORPH_HELLO_INTERVAL;
    
    private final Map<String,String> confs;
    
    public ConfigOptionsHelper() {
    	this.confs = new HashMap<String, String>();
    	this.populateConfs();
	}
    
    private void populateConfs(){
    	confs.put(ConfigOptionsHelper.KEY_RRCLUSTER_IP, this.rrClusterIP);
    	confs.put(ConfigOptionsHelper.KEY_RRCLUSTER_PORT, this.rrClusterIP);
    	confs.put(ConfigOptionsHelper.KEY_AMORPH_GROUP, this.amorphGroup);
    	confs.put(ConfigOptionsHelper.KEY_AMORPH_PORT, this.amorphClusterPort);
    }
    
    protected String getConfigFileName() {
    	return this.configFile;
    }
    
    public Map<String, String> getAllConfigs(){
    	return Collections.unmodifiableMap(this.confs);
    }
    
}
