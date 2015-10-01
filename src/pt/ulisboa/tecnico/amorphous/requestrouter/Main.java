package pt.ulisboa.tecnico.amorphous.requestrouter;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;

import pt.ulisboa.tecnico.amorphous.requestrouter.internal.config.ConfigHelper;
import pt.ulisboa.tecnico.amorphous.requestrouter.internal.config.ConfigOptionsHelper;

public class Main {
	public static void main(String[] args) throws IOException {
		// Setup logger
		System.setProperty("org.restlet.engine.loggerFacadeClass", "org.restlet.ext.slf4j.Slf4jLoggerFacade");
		
		// Parse execution flags
		ConfigOptionsHelper coh = new ConfigOptionsHelper();
		CmdLineParser parser = new CmdLineParser(coh);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			parser.printUsage(System.out);
			System.exit(1);
		}
		
		// Bootstrap the whole process
		try {
			new RequestRouter(coh,false,true);
		} catch (NumberFormatException | InstantiationException e) {
			System.out.println(e.getClass().getSimpleName() + " occurred while starting RequestRouter: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}
