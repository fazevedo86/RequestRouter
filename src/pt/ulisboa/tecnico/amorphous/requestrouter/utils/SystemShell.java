package pt.ulisboa.tecnico.amorphous.requestrouter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemShell {

	private static final Logger logger = LoggerFactory.getLogger(SystemShell.class);
	
	public static List<String> executeCommand(String command) throws IOException, InterruptedException {
		ArrayList<String> output = new ArrayList<String>();
		String outputLine = "";
		
		Process cmd = Runtime.getRuntime().exec(command);
		if(cmd.waitFor() == 0){
			BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
			while ((outputLine = cmdOutput.readLine()) != null) {
				SystemShell.logger.info("SystemShell execution returned: " + outputLine);
				output.add(outputLine);
			}
			cmdOutput.close();
		} else {
			SystemShell.logger.error("System shell command execution did not return properly");
		}

		return output;
	}
}
