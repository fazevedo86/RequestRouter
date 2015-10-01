package pt.ulisboa.tecnico.amorphous.requestrouter.internal.shell;

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
		Process cmd;
		
		try{
			cmd = Runtime.getRuntime().exec(command);
		} catch(IOException e){
			SystemShell.logger.info(e.getClass().getSimpleName() + " occurred while executing command \"" + command + "\": " + e.getMessage());
			e.printStackTrace();
			return output;
		}
		
		BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
		while ((outputLine = cmdOutput.readLine()) != null) {
			output.add(outputLine);
		}
		
		cmd.waitFor();
		cmd.destroy();
		cmdOutput.close();
			
		if(cmd.waitFor() != 0){
			SystemShell.logger.error("Execution of command \"" + command + "\" did not return properly!");
		}

		return output;
	}
}
