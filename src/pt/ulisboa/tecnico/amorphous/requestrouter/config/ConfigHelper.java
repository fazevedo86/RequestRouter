package pt.ulisboa.tecnico.amorphous.requestrouter.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigHelper {
	private static final Logger logger = LoggerFactory.getLogger(ConfigHelper.class);
	
	Map<String,String> confs;
	
	public ConfigHelper(ConfigOptionsHelper conf) {
       this.parseConfigFile(conf);
	}
	
	private void parseConfigFile(ConfigOptionsHelper conf){
		Properties fileConfigs = new Properties();
		File configFile = new File(conf.getConfigFileName());
        
        if (configFile.exists() && configFile.isFile()) {
        	try (FileInputStream fis = new FileInputStream(configFile)) {
        		fileConfigs.load(fis);
            } catch (IOException | IllegalArgumentException e) {
            	logger.error("Failed to read configuration file: " + configFile.getPath());
            }
        	ConfigHelper.logger.info("Configuration loaded from " + configFile.getPath());
        }
        
        // Merge with defaults for missing configs
        this.mergeDefaultConfigs(fileConfigs, conf.getAllConfigs());
        
        ConfigHelper.logger.info("Computed configuration set: " + this.confs.toString());
	}

	private void mergeDefaultConfigs(Map<Object,Object> fileConfs, Map<String,String> defaultConfigs){
		this.confs = new HashMap<String, String>();
		this.confs.putAll(defaultConfigs);
		for(Object confKey : fileConfs.keySet()){
			this.confs.put((String)confKey, (String)fileConfs.get(confKey));
		}
	}
	
	public String getConfig(String confKey){
		return this.confs.get(confKey);
	}
}
