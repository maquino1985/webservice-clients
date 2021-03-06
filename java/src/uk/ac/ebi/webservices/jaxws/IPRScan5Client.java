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
 * InterProScan 5 (SOAP) web service Java client using JAX-WS.
 * ----------------------------------------------------------------------
 * Tested with:
 *   OpenJDK 1.6.0_30 on Ubuntu 12.04.
 * ====================================================================== */
package uk.ac.ebi.webservices.jaxws;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays; 
import java.util.Map;

import javax.xml.rpc.ServiceException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import org.apache.commons.cli.*;
import uk.ac.ebi.webservices.jaxws.stubs.iprscan5.*;

/** InterProScan 5 (SOAP) web service Java client using JAX-WS.
 * 
 * See:
 * <a href="http://www.ebi.ac.uk/Tools/webservices/services/pfa/iprscan5">http://www.ebi.ac.uk/Tools/webservices/services/pfa/iprscan5</a>
 * <a href="http://www.ebi.ac.uk/Tools/webservices/tutorials/java">http://www.ebi.ac.uk/Tools/webservices/tutorials/java</a>
 */
public class IPRScan5Client extends uk.ac.ebi.webservices.AbstractWsToolClient {
	/** Service proxy */
	private JDispatcherService srvProxy = null;
	/** Object factory for creating objects to be exchanged with the web service. */
	private ObjectFactory objFactory = new ObjectFactory();
	/** Client version/revision */
	private String revision = "$Revision$";
	/** Tool specific usage message */
	private static final String usageMsg = "InterProScan 5\n"
			+ "==============\n"
			+ "\n"
			+ "Identify protein family, domain and signal signatures in a protein sequence.\n"
			+ "\n"
			+ "For information see:\n"
			+ "- http://www.ebi.ac.uk/interpro/\n"
			+ "- http://www.ebi.ac.uk/Tools/webservices/services/pfa/iprscan5_soap\n"
			+ "\n"
			+ "[Required]\n"
			+ "\n"
			+ "  seqFile            : file : query sequence (\"-\" for STDIN, @filename for\n"
			+ "                              identifier list file)\n"
			+ "\n"
			+ "[Optional]\n"
			+ "\n"
			+ "      --appl         : str  : list of signature methods to run, comma, space or\n"
			+ "                              plus sign separated: see --paramDetail appl. \n"
			+ "      --goterms      :      : enable retrieval of GO terms\n"
			+ "      --nogoterms    :      : disable retrieval of GO terms\n"
			+ "      --pathways     :      : enable retrieval of pathway terms\n"
			+ "      --nopathways   :      : disable retrieval of pathway terms\n"
			+ "      --multifasta   :      : treat input as a set of fasta formatted sequences\n"
			+ "\n";

	/** Default constructor
	 * 
	 */
	public IPRScan5Client() {
		// Set the HTTP user agent string for (java.net) requests.
		this.setUserAgent();
	}
	
	/** <p>Get a user-agent string for this client.</p>
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

	/** Set the HTTP headers for web services requests via the JAX-WS service 
	 * proxy to specify the User-Agent and HTTP compression support. 
	 */
	private void setPortHttpHeaders() {
		printDebugMessage("setPortHttpHeaders", "Begin", 1);
		if(this.srvProxy != null) {
			Map<String, Object> ctxt = ((BindingProvider)this.srvProxy).getRequestContext();
			@SuppressWarnings("unchecked")
			Map<String, List<String>> httpHeaders = (Map<String, List<String>>)ctxt.get(MessageContext.HTTP_REQUEST_HEADERS);
			if(httpHeaders == null) httpHeaders = new HashMap<String, List<String>>();

			// Add custom HTTP headers.
			// User-agent.
			String clientUserAgent = getClientUserAgentString();
			if(clientUserAgent != null && clientUserAgent.length() > 0) {
				httpHeaders.put("User-Agent", Collections.singletonList(clientUserAgent));
			}
			// HTTP response compression.
			httpHeaders.put("Accept-Encoding", Collections.singletonList("gzip"));
			// Note: HTTP request compression not supported by the server.
			// Add the headers to the context.
			ctxt.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
		}
		printDebugMessage("setPortHttpHeaders", "End", 1);
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
		if(this.srvProxy == null) {
			JDispatcherService_Service service = null;
			if(this.getServiceEndPoint() != null) {
				try {
					service = new JDispatcherService_Service(new java.net.URL(this.getServiceEndPoint()), new javax.xml.namespace.QName("http://soap.jdispatcher.ebi.ac.uk", "JDispatcherService"));
				}
				catch(java.net.MalformedURLException ex) {
					System.err.println(ex.getMessage());
					System.err.println("Warning: problem with specified endpoint URL. Default endpoint used.");
					service = new JDispatcherService_Service();
				}
			}
			else {
				service = new JDispatcherService_Service();
			}
			this.srvProxy = service.getJDispatcherServiceHttpPort();
			this.setPortHttpHeaders();
		}
		printDebugMessage("srvProxyConnect", "End", 11);
	}

	/** Get the web service proxy so it can be called directly.
	 * 
	 * @return The web service proxy.
	 * @throws javax.xml.rpc.ServiceException
	 */
	public JDispatcherService getSrvProxy() throws javax.xml.rpc.ServiceException {
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
		retVal = this.srvProxy.getParameters().getId().toArray(new String[0]);
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
		WsParameterValues parameterList = paramDetail.getValues();
		if(parameterList == null) return;
		WsParameterValue[] valueList = parameterList.getValue().toArray(new WsParameterValue[0]);
		for(int i = 0; i < valueList.length; i++) {
			System.out.print(valueList[i].getValue());
			if(valueList[i].isDefaultValue()) {
				System.out.println("\tdefault");
			}
			else {
				System.out.println();
			}
			System.out.println("\t" + valueList[i].getLabel());
			WsProperties propertiesList = valueList[i].getProperties();
			if(propertiesList != null) {
				WsProperty[] valuePropertiesList = propertiesList.getProperty().toArray(new WsProperty[0]);
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
		WsResultTypes resultTypes = this.srvProxy.getResultTypes(jobId);
		if(resultTypes != null) {
			retVal = resultTypes.getType().toArray(new WsResultType[0]);
			printDebugMessage("getResultTypes", retVal.length + " result types", 2);
		}
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
					+ typeList[i].getLabel().getValue() + "\n\t"
					+ typeList[i].getDescription().getValue() + "\n\t"
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
		//clientPoll(jobid); // Wait for job to finish
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
		InputParameters params = objFactory.createInputParameters();
		// Tool specific options
		if (line.hasOption("appl")) {
			String tmpVal = line.getOptionValue("appl").replace(',', ' ').replace('+', ' ').replaceAll("\\s+", " ");
			String[] dbList = tmpVal.split(" ");
			List<String> applList = Arrays.asList(dbList);
			ArrayOfString applArray = objFactory.createArrayOfString();
			applArray.getString().addAll(applList);
			params.setAppl(objFactory.createInputParametersAppl(applArray));
		}
		else if (line.hasOption("app")) {
			String tmpVal = line.getOptionValue("app").replace(',', ' ').replace('+', ' ').replaceAll("\\s+", " ");
			String[] dbList = tmpVal.split(" ");
			List<String> applList = Arrays.asList(dbList);
			ArrayOfString applArray = objFactory.createArrayOfString();
			applArray.getString().addAll(applList);
			params.setAppl(objFactory.createInputParametersAppl(applArray));
		}
		/* if(line.hasOption("crc")) { // Ignored.
			params.setNocrc(objFactory.createInputParametersNocrc(new Boolean(false)));
		}
		else if (line.hasOption("nocrc")) {
			params.setNocrc(objFactory.createInputParametersNocrc(new Boolean(true)));
		} */
		if(line.hasOption("goterms")) {
			params.setGoterms(objFactory.createInputParametersGoterms(new Boolean(true)));
		}
		else if (line.hasOption("nogoterms")) {
			params.setGoterms(objFactory.createInputParametersGoterms(new Boolean(false)));
		}
		if (line.hasOption("pathways"))
			params.setPathways(objFactory.createInputParametersPathways(new Boolean(true)));
		else if (line.hasOption("nopathways"))
			params.setPathways(objFactory.createInputParametersPathways(new Boolean(false)));
		printDebugMessage("loadParams", "End", 1);
		return params;
	}
	
	/**
	 * Submit a job using the command-line information to construct the input.
	 * 
	 * @param cli
	 *            Command-line parameters.
	 * @param inputSeq
	 *            Data input.
	 * @throws ServiceException
	 * @throws IOException
	 */
	public String submitJobFromCli(CommandLine cli, String inputSeq)
			throws ServiceException, IOException {
		printDebugMessage("submitJobFromCli", "Begin", 1);
		// Create job submission parameters from command-line
		InputParameters params = this.loadParams(cli);
		params.setSequence(objFactory.createInputParametersSequence(inputSeq));
		// Submit the job
		String email = null, title = null;
		if (cli.hasOption("email"))
			email = cli.getOptionValue("email");
		if (cli.hasOption("title"))
			title = cli.getOptionValue("title");
		String jobid = this.runApp(email, title, params);
		// Asynchronous submission.
		if (cli.hasOption("async")) {
			System.out.println(jobid); // Output the job id.
			System.err
					.println("To get status: java -jar IPRScan5_JAXWS.jar --status --jobid "
							+ jobid);
		}
		//  Parallel submission mode.
		else if(cli.hasOption("maxJobs") && Integer.parseInt(cli.getOptionValue("maxJobs")) > 1) {
			this.printProgressMessage(jobid, 1);
		}
		// Simulate synchronous submission, serial mode.
		else {
			this.clientPoll(jobid);
			this.getResults(jobid, cli);
		}
		this.printDebugMessage("submitJobFromCli", "End", 1);
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
		options.addOption("multifasta", "multifasta", false,
				"Multiple fasta sequence input");
		options.addOption("maxJobs", "maxJobs", true,
				"Maximum number of concurrent jobs");
		options.addOption("useSeqId", "useSeqId", false,
				"Use sequence identifiers for file names");
		// Application specific options
		options.addOption("appl", "appl", true, "Signature methods");
		options.addOption("app", "app", true, "Signature methods");
		options.addOption("goterms", "goterms", false, "Enable GO terms");
		options.addOption("nogoterms", "nogoterms", false, "Disable GO terms");
		options.addOption("pathways", "pathways", false, "Enable pathway terms");
		options.addOption("nopathways", "nopathways", false, "Disable pathway terms");
		options.addOption("sequence", true,
				"sequence file or datbase entry database:acc.no");
		// Compatibility options.
		options.addOption("crc", "crc", false, "Enable CRC look-up (ignored)");
		options.addOption("nocrc", "nocrc", false, "Disable CRC look-up (ignored)");

		CommandLineParser cliParser = new GnuParser(); // Create the command line parser
		IPRScan5Client client = null;
		try {
			// Create an instance of the client
			client = new IPRScan5Client();
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
					client.clientPoll(jobid);
					boolean resultContainContent = client.getResults(jobid, cli);
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
					client.clientPoll(jobid);
					client.printResultTypes(jobid);
				}
				// Unknown...
				else {
					System.err.println("Error: jobid specified without releated action option");
					printUsage();
					exitVal = 2;
				}
			}
			// Submit a job
			else if(cli.hasOption("email") && (cli.hasOption("sequence") || cli.getArgs().length > 0)) {
				// Input sequence, data file or entry identifier.
				String dataOption = (cli.hasOption("sequence")) ? cli
						.getOptionValue("sequence") : cli.getArgs()[0];
				// Multi-fasta sequence input.
				if (cli.hasOption("multifasta")) {
					client.multifastaSubmitCli(dataOption, cli);
				}
				// Entry identifier list.
				else if (dataOption.startsWith("@")) {
					client.idlistSubmitCli(dataOption, cli);
				}
				// Submit a job
				else {
					client.printDebugMessage("main", "Mode: sequence", 11);
					client.submitJobFromCli(cli, new String(client
							.loadData(dataOption)));
				}
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
			if(client != null && client.getDebugLevel() > 0) {
				e.printStackTrace();
			}
			exitVal = 3;
		}
		System.exit(exitVal);
	}
}
