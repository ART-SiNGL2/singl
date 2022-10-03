package singl.client;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;

import singl.client.util.Email;
import singl.client.util.EmailBody;

/**
 * This is the main point of interface with the SiNGL matching product.
 * 
 * @author sgoyal
 *
 */
public class Client implements Serializable {
	private Arguments arguments;
	private ISiNGL siNGL;
	private ClientOptions options;

	public static final Log LOG = LogFactory.getLog(Client.class);


	/**
	 * Construct a client to SiNGL using provided arguments and spark master.
	 * If running locally, set the master to local.
	 * 
	 * @param args
	 *            - arguments for training and matching
	 * @throws SiNGLClientException
	 *             if issue connecting to master
	 */
	
	
	public Client(Arguments args, ClientOptions options) throws SiNGLClientException {
		this.options = options;
		try {
			buildAndSetArguments(args, options);
			printAnalyticsBanner(arguments.getCollectMetrics());
			setSiNGL(args, options);					
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new SiNGLClientException("An error has occured while setting up the client" + e.getMessage());
		}
	}

	public void setSiNGL(Arguments args, ClientOptions options) throws Exception{
		JavaSparkContext.jarOfClass(ISiNGLFactory.class);
		ISiNGLFactory zf = (ISiNGLFactory) Class.forName("singl.SFactory").newInstance();
		setSiNGL(zf.get(SiNGLOptions.getByValue(options.get(ClientOptions.PHASE).value.trim())));
	}

	public void setSiNGL(ISiNGL siNGL) {
		this.siNGL = siNGL; 
	}

	public void buildAndSetArguments(Arguments args, ClientOptions options) {
		int jobId = new Long(System.currentTimeMillis()).intValue();
		if (options.get(options.JOBID)!= null) {
			LOG.info("Using job id from command line");
			String j = options.get(options.JOBID).value;
			jobId = Integer.parseInt(j);
			args.setJobId(jobId);
		}
		else if (args.getJobId() != -1) {
			jobId = args.getJobId();
		}
		
		//override value of siNGLDir passed from command line
		if (options.get(options.SINGL_DIR)!= null) {
			LOG.info("Using SiNGL dir from command line");
		
			String j = options.get(options.SINGL_DIR).value;
			args.setSiNGLDir(j);
		}
		if (options.get(options.MODEL_ID)!= null) {
			LOG.info("Using model id from command line");
		
			String j = options.get(options.MODEL_ID).value;
			args.setModelId(j);
		}
		if (options.get(options.COLLECT_METRICS)!= null) {
			String j = options.get(options.COLLECT_METRICS).value;
			args.setCollectMetrics(Boolean.valueOf(j));
		}
		if (options.get(ClientOptions.SHOW_CONCISE)!= null) {
			String j = options.get(ClientOptions.SHOW_CONCISE).value;
			args.setShowConcise(Boolean.valueOf(j));
		}
		setArguments(args);
	}
	
	public static void printBanner() {
		String versionStr = "2.0-0.3.3";
		LOG.info("");
		LOG.info("********************************************************");
		LOG.info("*                  SiGNL v2.0-0.3.3                    *");
		LOG.info("*                   (C) 2021 SiNGL                     *");
		LOG.info("********************************************************");
		LOG.info("");
		LOG.info("using: SiNGL v" + versionStr);
		LOG.info("");
	}
	
	public static void printAnalyticsBanner(boolean collectMetrics) {
		if(collectMetrics) {
			LOG.info("");
			LOG.info("**************************************************************************");
			LOG.info("*            ** Note about analytics collection by SiNGL **              *");
			LOG.info("*                                                                        *");
			LOG.info("*  Please note that SiNGL captures a few metrics about application's     *");
			LOG.info("*  runtime parameters. However, no user's personal data or application   *");
			LOG.info("*  data is captured. If you want to switch off this feature, please      *");
			LOG.info("*  set the flag collectMetrics to false in config.                       *");
			LOG.info("**************************************************************************");
			LOG.info("");
		}
		else {
			LOG.info("");
			LOG.info("********************************************************");
			LOG.info("*    SiNGL is not collecting any analytics data        *");
			LOG.info("********************************************************");
			LOG.info("");
		}
	}

	public static void main(String... args) {
		printBanner();
		Client client = null;
		ClientOptions options = null;
		try {
			for (String a: args) LOG.debug("args " + a);
			options = new ClientOptions(args);
		
			if (options.has(options.HELP) || options.has(options.HELP1) || options.get(ClientOptions.PHASE) == null) {
				LOG.warn(options.getHelp());
				System.exit(0);
			}
			String phase = options.get(ClientOptions.PHASE).value.trim();
			SiNGLOptions.verifyPhase(phase);
			Arguments arguments = null;
			if (options.get(ClientOptions.CONF).value.endsWith("json")) {
					arguments = Arguments.createArgumentsFromJSON(options.get(ClientOptions.CONF).value, phase);
			}
			else if (options.get(ClientOptions.CONF).value.endsWith("env")) {
				arguments = Arguments.createArgumentsFromJSONTemplate(options.get(ClientOptions.CONF).value, phase);
			}
			else {
				arguments = Arguments.createArgumentsFromJSONString(options.get(ClientOptions.CONF).value, phase);
			}

			client = new Client(arguments, options);	
			client.init();
			client.execute();
			client.postMetrics();
			LOG.warn("SiNGL processing has completed");				
		} 
		catch(SiNGLClientException e) {
			if (options != null) {
				Email.email(options.get(ClientOptions.EMAIL).value, new EmailBody("Error running SiNGL job",
					"SiNGL Error ",
					e.getMessage()));
			}
			LOG.warn("Apologies for this message. SiNGL has encountered an error. "
					+ e.getMessage());
			if (LOG.isDebugEnabled()) e.printStackTrace();
		}
		catch( Throwable e) {
			if (options != null && options.get(ClientOptions.EMAIL) != null) {
				Email.email(options.get(ClientOptions.EMAIL).value, new EmailBody("Error running SiNGL job",
					"SiNGL Error ",
					e.getMessage()));
			}
			LOG.warn("Apologies for this message. SiNGL has encountered an error. "
					+ e.getMessage());
			if (LOG.isDebugEnabled()) e.printStackTrace();
		}
		finally {
			try {
				if (client != null) {
					client.stop();
				}
			}
			catch(SiNGLClientException e) {
				if (options != null) {
					Email.email(options.get(ClientOptions.EMAIL).value, new EmailBody("Error running SiNGL job",
						"SiNGL Error ",
						e.getMessage()));
				}
			}
		}
	}

	public void init() throws SiNGLClientException {
		siNGL.init(getArguments(), "");
	}
	
	
	/**
	 * Stop the Spark job running context
	 */
	public void stop() throws SiNGLClientException{
		siNGL.cleanup();
	}

	public Arguments getArguments() {
		return arguments;
	}

	public void execute() throws SiNGLClientException {
		siNGL.execute();
 	}

	public void postMetrics() throws SiNGLClientException {
		siNGL.postMetrics();
	}

	public void setArguments(Arguments args) {
		this.arguments = args;				
	}

	public ClientOptions getOptions() {
		return options;
	}

	public void setOptions(ClientOptions options) {
		this.options = options;
	}
}