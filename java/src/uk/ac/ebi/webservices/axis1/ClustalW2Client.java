/* $Id$
 * ======================================================================
 * 
 * Copyright 2010-2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * JDispatcher ClustalW2 (SOAP) web service Java client using Axis 1.x.
 * ----------------------------------------------------------------------
 * Tested with:
 *   Sun Java 1.5.0_17 with Apache Axis 1.4 on CentOS 5.2.
 * ====================================================================== */
package uk.ac.ebi.webservices.axis1;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.cli.*;
import uk.ac.ebi.webservices.axis1.stubs.clustalw2.*;

/** <p>JDispatcher ClustalW2 (SOAP) web service Java client using Apache 
 * Axis 1.x.</p>
 * 
 * <p>See:</p>
 * <ul>
 * <li><a href="http://www.ebi.ac.uk/Tools/webservices/services/msa/clustalw2_soap">http://www.ebi.ac.uk/Tools/webservices/services/msa/clustalw2_soap</a></li>
 * <li><a href="http://www.ebi.ac.uk/Tools/webservices/tutorials/06_programming/java">http://www.ebi.ac.uk/Tools/webservices/tutorials/06_programming/java</a></li>
 * <li><a href="http://ws.apache.org/axis/">http://ws.apache.org/axis/</a></li>
 * </ul>
 */
public class ClustalW2Client extends uk.ac.ebi.webservices.AbstractWsToolClient {
	/** Proxy object for web service. */
	private JDispatcherService_PortType srvProxy = null;
	/** Client version/revision for use in user-agent string. */
	private String revision = "$Revision$";
	/** Tool specific usage for help. */
	private static final String usageMsg = "ClustalW2\n"
		+ "=========\n"
		+ "\n"
		+ "General purpose multiple sequence alignment program for DNA or protein.\n"
		+ "\n"
		+ "ClustalW uses a progressive alignment approach that consists of three stages:\n"
		+ "\n"
		+ "1. Pairwise (fast or slow) alignment of input sequences.\n"
		+ "2. Generation of a guide tree from the pairwise alignments.\n"
		+ "3. Multiple alignment using the guide tree to determine how sequences are\n" 
		+ "added to the alignment.\n"
		+ "\n"    
		+ "[Required]\n"
		+ "\n"
		+ "  seqFile            : file : sequences to align (\"-\" for STDIN)\n"
		+ "\n"
		+ "[Optional]\n"
		+ "\n"
		+ "  -l, --alignment    : str  : pairwise alignment method, \n"
		+ "                              see --paramDetail alignment\n"
		+ "      --type         : str  : sequence type, see --paramDetail type\n"
		+ "  -o, --output       : str  : alignment format, see --paramDetail output\n"
		+ "  -r, --outorder     : str  : order of sequences in alignment,\n" 
		+ "                              see --paramDetail outorder\n"
		+ "\n"
		+ "[Fast Pairwise Alignment]\n"
		+ "\n"
		+ "  -k, --ktup         : int  : word size\n"
		+ "  -w, --window       : int  : window size\n"
		+ "  -s, --score        : str  : score type, see --paramDetail score\n"
		+ "  -d, --topdiags     : int  : number of best diags.\n"
		+ "  -p, --pairgap      : int  : gap penalty\n"
		+ "\n"
		+ "[Slow Pairwise Alignment]\n"
		+ "\n"
		+ "      --pwmatrix     : str  : Protein scoring matrix,\n"
		+ "                              see --paramDetail pwmatrix\n"
		+ "      --pwdnamatrix  : str  : DNA/RNA scoring matrix,\n"
		+ "                              see --paramDetail pwdnamatrix\n"
		+ "      --pwgapopen    : int  : gap creation penalty\n"
		+ "      --pwgapext     : real : gap extension penalty\n"
		+ "\n"
		+ "[Multiple Alignment]\n"
		+ "\n"
		+ "  -m, --matrix       : str  : Protein scoring matrix,\n"
		+ "                              see --paramDetail matrix\n"
		+ "      --dnamatrix    : str  : DNA/RNA scoring matrix, \n"
		+ "                              see --paramDetail dnamatrix\n"
		+ "  -g, --gapopen      : int  : gap creation penalty\n"
		+ "      --noendgaps    : bool : no end gap separation penalty\n"
		+ "  -x, --gapext       : real : gap extension penalty\n"
		+ "  -y, --gapdist      : int  : gap seperation penalty\n"
		+ "  -i, --iteration    : str  : iteration strategy, see --paramDetail iteration\n" 
		+ "  -N, --numiter      : int  : maximum number of iterations\n"
		+ "      --clustering   : str  : clustering method for guide tree\n";

	/** Default constructor.
	 */
	public ClustalW2Client() {
		// Set the HTTP user agent string for (java.net) requests.
		this.setUserAgent();
	}
	
	/** <p>Get a user-agent string for this client.</p>
	 * 
	 * <p><b>Note</b>: this affects all java.net based requests, but not the 
	 * Axis requests. The user-agent used by Axis is set from the 
	 * /org/apache/axis/i18n/resource.properties file included in the Axis 
	 * JAR.</p>
	 * 
	 * @return Client user-agent string.
	 */
	protected String getClientUserAgentString() {
		printDebugMessage("getClientUserAgent", "Begin", 11);
		String clientVersion = this.revision.substring(11, this.revision.length() - 2);
		String clientUserAgent = "EBI-Sample-Client/" + clientVersion 
			+ " (" + this.getClass().getName() + "; " 
			+ System.getProperty("os.name") + ")";
		printDebugMessage("getClientUserAgent", "End", 11);
		return clientUserAgent;
	}

	/** Print usage message. */
	private static void printUsage() {
		System.out.println(usageMsg);
		printGenericOptsUsage();
	}
	
	/** Ensure that a service proxy is available to call the web service.
	 * 
	 * @throws ServiceException
	 */
	protected void srvProxyConnect() throws ServiceException {
		printDebugMessage("srvProxyConnect", "Begin", 11);
		if (this.srvProxy == null) {
			JDispatcherService_Service service = new JDispatcherService_ServiceLocatorExtended();
			if (this.getServiceEndPoint() != null) {
				try {
					this.srvProxy = service
							.getJDispatcherServiceHttpPort(new java.net.URL(
									this.getServiceEndPoint()));
				} catch (java.net.MalformedURLException ex) {
					System.err.println(ex.getMessage());
					System.err
							.println("Warning: problem with specified endpoint URL. Default endpoint used.");
					this.srvProxy = service.getJDispatcherServiceHttpPort();
				}
			} else {
				this.srvProxy = service.getJDispatcherServiceHttpPort();
			}
		}
		printDebugMessage("srvProxyConnect", "End", 11);
	}
	
	/** Wrapper for JDispatcherService_ServiceLocator to enable HTTP 
	 * compression.
	 * 
	 * Compression requires Commons HttpClient and a client-config.wsdd which 
	 * specifies that Commons HttpClient should be used as the HTTP transport.
	 * See http://wiki.apache.org/ws/FrontPage/Axis/GzipCompression.
	 */
	private class JDispatcherService_ServiceLocatorExtended extends JDispatcherService_ServiceLocator {
		private static final long serialVersionUID = 1L;

		public Call createCall() throws ServiceException {
			Call call = super.createCall();
			// Enable response compression.
			call.setProperty(HTTPConstants.MC_ACCEPT_GZIP, Boolean.TRUE);
			return call;
		}
	}

	/** Get the web service proxy so it can be called directly.
	 * 
	 * @return The web service proxy.
	 * @throws javax.xml.rpc.ServiceException
	 */
	public JDispatcherService_PortType getSrvProxy() throws javax.xml.rpc.ServiceException {
		printDebugMessage("getSrvProxy", "", 1);
		this.srvProxyConnect(); // Ensure the service proxy exists
		return this.srvProxy;
	}
	
	/** Get list of tool parameter names.
	 * 
	 * @return String array containing list of parameter names
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	public String[] getParams() throws ServiceException, RemoteException {
		printDebugMessage("getParams", "Begin", 1);
		String[] retVal = null;
		this.srvProxyConnect(); // Ensure the service proxy exists
		retVal = this.srvProxy.getParameters();
		printDebugMessage("getParams", retVal.length + " params", 2);
		printDebugMessage("getParams", "End", 1);
		return retVal;
	}

	/** Get detailed information about the specified tool parameter.
	 * 
	 * @param paramName Tool parameter name
	 * @return Object describing tool parameter
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	public WsParameterDetails getParamDetail(String paramName) throws ServiceException, RemoteException {
		printDebugMessage("getParamDetail", paramName, 1);
		this.srvProxyConnect(); // Ensure the service proxy exists
		return this.srvProxy.getParameterDetails(paramName);
	}

	/** Print detailed information about a tool parameter.
	 * 
	 * @param paramName Name of the tool parameter to get information for.
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	protected void printParamDetail(String paramName) throws RemoteException, ServiceException {
		printDebugMessage("printParamDetail", "Begin", 1);
		WsParameterDetails paramDetail = getParamDetail(paramName);
		// Print object
		System.out.println(paramDetail.getName() + "\t" + paramDetail.getType());
		System.out.println(paramDetail.getDescription());
		WsParameterValue[] valueList = paramDetail.getValues();
		if(valueList!=null) {
			for(int i = 0; i < valueList.length; i++) {
				System.out.print(valueList[i].getValue());
				if(valueList[i].isDefaultValue()) {
					System.out.println("\tdefault");
				}
				else {
					System.out.println();
				}
				System.out.println("\t" + valueList[i].getLabel());
				WsProperty[] valuePropertiesList = valueList[i].getProperties();
				if(valuePropertiesList != null) {
					for(int j = 0; j < valuePropertiesList.length; j++) {
						System.out.println("\t" + valuePropertiesList[j].getKey() + "\t" + valuePropertiesList[j].getValue());
					}
				}
			}
		}
		printDebugMessage("printParamDetail", "End", 1);
	}

	/** Get the status of a submitted job given its job identifier.
	 * 
	 * @param jobid The job identifier.
	 * @return Job status as a string.
	 * @throws IOException
	 * @throws ServiceException
	 */
	public String checkStatus(String jobid) throws IOException, ServiceException {
		printDebugMessage("checkStatus", jobid, 1);
		this.srvProxyConnect(); // Ensure the service proxy exists
		return this.srvProxy.getStatus(jobid);
	}

	/** Get details of the available result types for a job.
	 * 
	 * @param jobId Job identifier to check for results types.
	 * @return Array of objects describing result types.
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	public WsResultType[] getResultTypes(String jobId) throws ServiceException, RemoteException {
		printDebugMessage("getResultTypes", "Begin", 1);
		printDebugMessage("getResultTypes", "jobId: " + jobId , 2);
		WsResultType[] retVal = null;
		this.srvProxyConnect(); // Ensure the service proxy exists
		retVal = this.srvProxy.getResultTypes(jobId);
		printDebugMessage("getResultTypes", retVal.length + " result types", 2);
		printDebugMessage("getResultTypes", "End", 1);
		return retVal;
	}
	
	/** Print details of the available result types for a job.
	 * 
	 * @param jobId Job identifier to check for result types.
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	protected void printResultTypes(String jobId) throws ServiceException, RemoteException {
		printDebugMessage("printResultTypes", "Begin", 1);
		WsResultType[] typeList = getResultTypes(jobId);
		for(int i = 0; i < typeList.length; i++) {
			System.out.print(
					typeList[i].getIdentifier() + "\n\t"
					+ typeList[i].getLabel() + "\n\t"
					+ typeList[i].getDescription() + "\n\t"
					+ typeList[i].getMediaType() + "\n\t"
					+ typeList[i].getFileSuffix() + "\n"
					);
		}
		printDebugMessage("printResultTypes", "End", 1);
	}
	
	/** Get the results for a job and save them to files.
	 * 
	 * @param jobid The job identifier.
	 * @param outfile The base name of the file to save the results to. If
	 * null the jobid will be used.
	 * @param outformat The name of the data format to save, e.g. toolraw 
	 * or toolxml. If null all available data formats will be saved.
	 * @return Array of filenames
	 * @throws IOException
	 * @throws javax.xml.rpc.ServiceException
	 */
	public String[] getResults(String jobid, String outfile, String outformat) throws IOException, javax.xml.rpc.ServiceException {
		printDebugMessage("getResults", "Begin", 1);
		printDebugMessage("getResults", "jobid: " + jobid + " outfile: " + outfile + " outformat: " + outformat, 2);
		String[] retVal = null;
		this.srvProxyConnect(); // Ensure the service proxy exists
		clientPoll(jobid); // Wait for job to finish
		// Set the base name for the output file.
		String basename = (outfile != null) ? outfile : jobid;
		// Get result types
		WsResultType[] resultTypes = getResultTypes(jobid);
		int retValN = 0;
		if(outformat == null) {
			retVal = new String[resultTypes.length];
		} else {
			retVal = new String[1];
		}
		for(int i = 0; i < resultTypes.length; i++) {
			printProgressMessage("File type: " + resultTypes[i].getIdentifier(), 2);
			// Get the results
			if(outformat == null || outformat.equals(resultTypes[i].getIdentifier())) {
				byte[] resultbytes = this.srvProxy.getResult(jobid, resultTypes[i].getIdentifier(), null);
				if(resultbytes == null) {
					System.err.println("Null result for " + resultTypes[i].getIdentifier() + "!");
				}
				else {
					printProgressMessage("Result bytes length: " + resultbytes.length, 2);
					// Write the results to a file
					String result = new String(resultbytes);
					if(basename.equals("-")) { // STDOUT
						if(resultTypes[i].getMediaType().startsWith("text")) { // String
							System.out.print(result);
						}
						else { // Binary
							System.out.print(resultbytes);
						}
					}
					else { // File
						String filename = basename + "." + resultTypes[i].getIdentifier() + "." + resultTypes[i].getFileSuffix();
						if(resultTypes[i].getMediaType().startsWith("text")) { // String
							writeFile(new File(filename), result);
						}
						else { // Binary
							writeFile(new File(filename), resultbytes);
						}
						retVal[retValN] = filename;
						retValN++;
					}
				}
			}
		}
		printDebugMessage("getResults", retVal.length + " file names", 2);
		printDebugMessage("getResults", "End", 1);
		return retVal;
	}

	/** Submit a job to the service.
	 * 
	 * @param params Input parameters for the job.
	 * @param content Data to run the job on.
	 * @return The job identifier.
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	public String runApp(String email, String title, InputParameters params) throws RemoteException, ServiceException {
		printDebugMessage("runApp", "Begin", 1);
		printDebugMessage("runApp", "email: " + email + " title: " + title, 2);
		printDebugMessage("runApp", "params:\n" + objectFieldsToString(params), 2);
		String jobId = null;
		this.srvProxyConnect(); // Ensure the service proxy exists
		jobId = srvProxy.run(email, title, params);
		printDebugMessage("runApp", "jobId: " + jobId, 2);
		printDebugMessage("runApp", "End", 1);
		return jobId;
	}

	/** Populate input parameters structure from command-line options.
	 * 
	 * @param line Command line options
	 * @return input Input parameters structure for use with runApp().
	 * @throws IOException
	 */
	public InputParameters loadParams(CommandLine line) throws IOException {
		printDebugMessage("loadParams", "Begin", 1);
		InputParameters params = new InputParameters();
		// Tool specific options
		if (line.hasOption("l")) params.setAlignment(line.getOptionValue("l"));
		if (line.hasOption("type")) params.setType(line.getOptionValue("type"));
		if (line.hasOption("k")) params.setKtup(new Integer(line.getOptionValue("k")));
		if (line.hasOption("w")) params.setWindow(new Integer(line.getOptionValue("w")));
		if (line.hasOption("s")) params.setScore(line.getOptionValue("s"));
		if (line.hasOption("d")) params.setTopdiags(new Integer(line.getOptionValue("d")));
		if (line.hasOption("p")) params.setPairgap(new Integer(line.getOptionValue("p")));
		if (line.hasOption("pwmatrix")) params.setPwmatrix(line.getOptionValue("pwmatrix"));
		if (line.hasOption("pwdnamatrix")) params.setPwdnamatrix(line.getOptionValue("pwdnamatrix"));
		if (line.hasOption("pwgapopen")) params.setPwgapopen(new Integer(line.getOptionValue("pwgapopen"))); 
		if (line.hasOption("pwgapext")) params.setPwgapext(new Float(line.getOptionValue("pwgapext"))); 
		if (line.hasOption("m")) params.setMatrix(line.getOptionValue("m"));
		if (line.hasOption("dnamatrix")) params.setDnamatrix(line.getOptionValue("dnamatrix"));
		if (line.hasOption("g")) params.setGapopen(new Integer(line.getOptionValue("g")));
		if (line.hasOption("noendgaps")) params.setNoendgaps(new Boolean(true));
		if (line.hasOption("x")) params.setGapext(new Float(line.getOptionValue("x")));
		if (line.hasOption("y")) params.setGapdist(new Integer(line.getOptionValue("y")));
		if (line.hasOption("i")) params.setIteration(line.getOptionValue("i"));
		if (line.hasOption("N")) params.setNumiter(new Integer(line.getOptionValue("N")));
		if (line.hasOption("clustering")) params.setClustering(line.getOptionValue("clustering"));
		if (line.hasOption("o")) params.setOutput(line.getOptionValue("o"));
		if (line.hasOption("r")) params.setOutorder(line.getOptionValue("r"));
		printDebugMessage("loadParams", "End", 1);
		return params;
	}

	/**
	 * Submit a job using the command-line information to construct the input.
	 * 
	 * @param cli
	 *            Command-line parameters.
	 * @param inputData
	 *            Data input.
	 * @throws ServiceException
	 * @throws IOException
	 */
	public String submitJobFromCli(CommandLine cli, String inputData)
			throws ServiceException, IOException {
		// Create job submission parameters from command-line
		InputParameters params = this.loadParams(cli);
		params.setSequence(inputData);
		// Submit the job
		String email = null, title = null;
		if (cli.hasOption("email"))
			email = cli.getOptionValue("email");
		if (cli.hasOption("title"))
			title = cli.getOptionValue("title");
		String jobid = this.runApp(email, title, params);
		// For asynchronous mode
		if (cli.hasOption("async")) {
			System.out.println(jobid); // Output the job id.
			System.err
					.println("To get status: java -jar ClustalW2_Axis1.jar --status --jobid "
							+ jobid);
		} else {
			// In synchronous mode try to get the results
			this.printProgressMessage(jobid, 1);
			String[] resultFilenames = this
					.getResults(jobid, cli.getOptionValue("outfile"), cli
							.getOptionValue("outformat"));
			for (int i = 0; i < resultFilenames.length; i++) {
				if (resultFilenames[i] != null) {
					System.out.println("Wrote file: " + resultFilenames[i]);
				}
			}
		}
		return jobid;
	}

	/** Entry point for running as an application.
	 * 
	 * @param args list of command-line options
	 */
	public static void main(String[] args) {
		int exitVal = 0; // Exit value
		int argsLength = args.length; // Number of command-line arguments

		// Configure the command-line options
		Options options = new Options();
		// Common options for EBI clients
		addGenericOptions(options);
		// Application specific options
		options.addOption("l", "alignment", true, "Pairwise alignment method");
		options.addOption("type", true, "Sequence type");
		options.addOption("k", "ktup", true, "Word size");
		options.addOption("w", "window", true, "Window size");
		options.addOption("s", "score", true, "Pairwise score representation");
		options.addOption("d", "topdiags", true, "Number of best diags.");
		options.addOption("p", "pairgap", true, "Gap penalty (pairwise)");
		options.addOption("pwmatrix", "pwmatrix", true, "Protein matrix (pairwise)");
		options.addOption("pwdnamatrix", "pwdnamatrix", true, "DNA matrix (pairwise)");
		options.addOption("pwgapopen", "pwgapopen", true, "Gap open penalty (pairwise)");
		options.addOption("pwgapext", "pwgapext", true, "Gap extension penalty (pairwise)");
		options.addOption("m", "matrix", true, "Protein scoring matrix (multi)");
		options.addOption("dnamatrix", "dnamatrix", true, "DNA scoring matrix (multi)");
		options.addOption("g", "gapopen", true, "Gap creation penalty (multi)");
		options.addOption("noendgaps", false, "Terminal gaps scored");
		options.addOption("x", "gapext", true, "Gap extension penalty (multi)");
		options.addOption("y", "gapdist", true, "Gap separation penalty (multi)");
		options.addOption("i", "iteration", true, "Iteration type");
		options.addOption("N", "numiter", true, "Number of iterations");
		options.addOption("clustering", true, "Clustering method for guide tree");
		options.addOption("o", "output", true, "Alignment format");
		options.addOption("r", "outorder", true, "Order of sequences in alignment");
		options.addOption("sequence", true, "Input sequences/alignment");

		CommandLineParser cliParser = new GnuParser(); // Create the command line parser    
		// Create an instance of the client
		ClustalW2Client client = new ClustalW2Client();
		try {
			// Parse the command-line
			CommandLine cli = cliParser.parse(options, args);
			// User asked for usage info
			if(argsLength == 0 || cli.hasOption("help")) {
				printUsage();
				System.exit(0);
			}
			// Modify output level according to the quiet and verbose options
			if(cli.hasOption("quiet")) {
				client.outputLevel--;
			}
			if(cli.hasOption("verbose")) {
				client.outputLevel++;
			}
			// Set debug level
			if(cli.hasOption("debugLevel")) {
				client.setDebugLevel(Integer.parseInt(cli.getOptionValue("debugLevel")));
			}
			// Alternative service endpoint
			if(cli.hasOption("endpoint")) {
				client.setServiceEndPoint(cli.getOptionValue("endpoint"));
			}
			// Tool meta-data
			// List parameters
			if(cli.hasOption("params")) {
				client.printParams();
			}
			// Details of a parameter
			else if(cli.hasOption("paramDetail")) {
				client.printParamDetail(cli.getOptionValue("paramDetail"));
			}
			// Job related actions
			else if(cli.hasOption("jobid")) {
				String jobid = cli.getOptionValue("jobid");
				// Get results for job
				if(cli.hasOption("polljob")) {                
					String[] resultFilenames = client.getResults(jobid, cli.getOptionValue("outfile"), cli.getOptionValue("outformat"));
					boolean resultContainContent = false;
					for(int i = 0; i < resultFilenames.length; i++) {
						if(resultFilenames[i] != null) {
							System.out.println("Wrote file: " + resultFilenames[i]);
							resultContainContent = true;
						}
					}
					if (resultContainContent == false) {
						System.err.println("Error: requested result type " + cli.getOptionValue("outformat") + " not available!");
					}
				}
				// Get status of job
				else if(cli.hasOption("status")) {
					System.out.println(client.checkStatus(jobid));
				}
				// Get result types for job
				else if(cli.hasOption("resultTypes")) {
					client.printResultTypes(jobid);
				}
				// Unknown...
				else {
					System.err.println("Error: jobid specified without related action option");
					printUsage();
					exitVal = 2;
				}
			}
			// Submit a job
			else if(cli.hasOption("email") && (cli.hasOption("sequence") || cli.getArgs().length > 0)) {
				String dataOption = (cli.hasOption("sequence")) ? cli.getOptionValue("sequence") : cli.getArgs()[0];
				client.submitJobFromCli(cli, new String(client
						.loadData(dataOption)));
			}
			// Unknown action
			else {
				System.err.println("Error: unknown combination of arguments. See --help.");
				exitVal = 2;
			}
		}
		catch(UnrecognizedOptionException ex) {
			System.err.println("ERROR: " + ex.getMessage());
			printUsage();
			exitVal = 1;
		}
		// Catch all exceptions
		catch(Exception e) {
			System.err.println ("ERROR: " + e.getMessage());
			//if(client.getDebugLevel() > 0) {
				e.printStackTrace();
			//}
			exitVal = 3;
		}
		System.exit(exitVal);
	}
}
