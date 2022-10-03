package singl.client;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;

import singl.client.pipe.Pipe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class helps supply match arguments to SiNGL. There are 3 basic steps
 * in any match process.
 * <ul>
 * <li>Defining - specifying information about data location, fields and our
 * notion of similarity.
 * <li>Training - making SiNGL learn the matching rules
 * <li>Matching - Running the models on entire dataset
 * </ul>
 * <p>
 * There is another step, creating labeled data, which can be used to create
 * training data if none is present. Let us cover them in greater detail through
 * an example.
 * <p>
 * We have some positive and negative labeled examples from which we want
 * SiNGL to learn. These are saved in
 * <p>
 * /path/to/training/data/positive.csv and
 * <p>
 * /path/to/training/data/negative.csv
 * <p>
 * Our actual data has colA,colB,colC,colD,colE with comma as the delimiter and
 * is saved at
 * <p>
 * /path/to/match/data.csv.
 * <p>
 * We want to match on colB and colD only, one of which is String and other is
 * int
 * <p>
 * Our program would look like
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	Arguments args = new Arguments();
 * 	args.setDelimiter(&quot;,&quot;);
 * 	args.setPositiveTrainingSamples(&quot;/path/to/training/data/positive.csv&quot;);
 * 	args.setNegativeTrainingSamples(&quot;/path/to/training/data/negative.csv&quot;);
 * 
 * 	FieldDefinition colB = new FieldDefinition(1, FieldClass.STRING,
 * 			FieldType.WORD);
 * 	FieldDefinition colD = new FieldDefinition(3, FieldClass.INTEGER,
 * 			FieldType.NUMERIC);
 * 
 * 	List&lt;FieldDefinition&gt; fields = new ArrayList&lt;FieldDefinition&gt;();
 * 	fields.add(colB);
 * 	fields.add(colD);
 * 	args.setFieldDefinition(fields);
 * 
 * 	args.setMatchData(&quot;/path/to/match/data.csv&quot;);
 * 
 * 	args.setSiNGLDir(&quot;/path/to/models&quot;);
 * 	args.setOutputDir(&quot;/path/to/match/output&quot;);
 * 
 * 	Client client = new Client(args, &quot;local&quot;);
 * 	client.train();
 * 	client.run();
 * }
 * </pre>
 */
@JsonInclude(Include.NON_NULL)
public class Arguments implements Serializable {

	// creates DriverArgs and invokes the main object
	Pipe[] output; 
	Pipe[] data;	
	//Pipe[] zinggInternal;
	String siNGLDir = "/tmp/singl";
	
	Pipe[] trainingSamples;
	List<FieldDefinition> fieldDefinition;
	int numPartitions = 10;
	float labelDataSampleSize = 0.01f;
	String modelId = "1";
	double threshold = 0.5d;
	int jobId = 1;
	boolean collectMetrics = true;
	boolean showConcise = false;
	
	private static final String ENV_VAR_MARKER_START = "$";
	private static final String ENV_VAR_MARKER_END = "$";
	private static final String ESC = "\\";
	private static final String PATTERN_ENV_VAR = ESC + ENV_VAR_MARKER_START + "(.+?)" + ESC + ENV_VAR_MARKER_END;

	public double getThreshold() {
		return threshold;
	}

	

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public static final Log LOG = LogFactory.getLog(Arguments.class);

	/**
	 * default constructor Use setters for setting individual fields
	 */
	public Arguments() {
	}
	
	/**
	 * Create arguments from a json file
	 * 
	 * @param filePath
	 *            json file containing arguments
	 * @return Arguments object populated through JSON
	 * @throws SiNGLClientException
	 *             in case of invlaid/wrong json/file not found
	 */
	public static final Arguments createArgumentsFromJSON(String filePath)
			throws SiNGLClientException {
		return Arguments.createArgumentsFromJSON(filePath, "match");
	}

	/**
	 * Create arguments from a json file
	 * 
	 * @param filePath
	 *            json file containing arguments
	 * @return Arguments object populated through JSON
	 * @throws SiNGLClientException
	 *             in case of invlaid/wrong json/file not found
	 */
	public static final Arguments createArgumentsFromJSON(String filePath, String phase)
			throws SiNGLClientException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
					true);
			LOG.warn("Config Argument is " + filePath);
			Arguments args = mapper.readValue(new File(filePath), Arguments.class);
			LOG.warn("phase is " + phase);
			checkValid(args, phase);
			return args;			
		} catch (Exception e) { 
			e.printStackTrace();
			throw new SiNGLClientException("Unable to parse the configuration at " + filePath + 
					". The error is " + e.getMessage());
		}
	}
	
	public static void checkValid(Arguments args, String phase) throws SiNGLClientException {
		if (phase.equals("train") || phase.equals("match") || phase.equals("trainMatch") || phase.equals("link")) {
			checkIsValid(args);
		}
		else if(phase.equals("seed") || phase.equals("seedDB")){
			checkIsValidForLabelling(args);
		}
		else if (!phase.equalsIgnoreCase("WEB")){
			checkIsValidForOthers(args);
		}
	}
	
	public static final Arguments createArgumentsFromJSONString(String data, String phase)
			throws SiNGLClientException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
					true);
			Arguments args = mapper.readValue(data, Arguments.class);
			LOG.warn("phase is " + phase);
			checkValid(args, phase);
			return args;			
		} catch (Exception e) {
			e.printStackTrace();
			throw new SiNGLClientException("Unable to parse the configuration at " + data + 
					". The error is " + e.getMessage());
		}
	}

	public static final Arguments createArgumentsFromJSONTemplate(String filePath, String phase)
			throws SiNGLClientException {
		try {
			LOG.warn("Config Argument is " + filePath);
			byte[] encoded = Files.readAllBytes(Paths.get(filePath));
			String template = new String(encoded, StandardCharsets.UTF_8);
			Map<String, String> env = System.getenv();
			String updatedJson = substituteVariables(template, env);
			Arguments args = createArgumentsFromJSONString(updatedJson, phase);
			return args;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SiNGLClientException("Unable to parse the configuration at " + filePath +
					". The error is " + e.getMessage());
		}
	}

	public static String substituteVariables(String template, Map<String, String> variables) throws SiNGLClientException {
		Pattern pattern = Pattern.compile(PATTERN_ENV_VAR);
		Matcher matcher = pattern.matcher(template);
		// StringBuilder cannot be used here because Matcher expects StringBuffer
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			if (variables.containsKey(matcher.group(1))) {
				String replacement = variables.get(matcher.group(1));
				if (replacement == null || replacement.equals("")) {
					throw new SiNGLClientException("The environment variable for " + ENV_VAR_MARKER_START
							+ matcher.group(1) + ENV_VAR_MARKER_END + " is not set or is empty string");
				}
				// quote to work properly with $ and {,} signs
				matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "null");
				LOG.warn("The variable " + ENV_VAR_MARKER_START + matcher.group(1) + ENV_VAR_MARKER_END
						+ " has been substituted");
			} else {
				throw new SiNGLClientException("The environment variable for " + ENV_VAR_MARKER_START + matcher.group(1)
						+ ENV_VAR_MARKER_END + " is not set");
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	public static final void writeArgumentstoJSON(String filePath, Arguments args) throws SiNGLClientException {
		try{
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
					true);
			mapper.registerModule(new DefaultScalaModule());
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), args);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SiNGLClientException("Unable to create arguments for the job");
		}
	}

	public int getNumPartitions() {
		return numPartitions;
	}

	public void setNumPartitions(int numPartitions) throws SiNGLClientException{
		if (numPartitions != -1 && numPartitions <= 0) 
			throw new SiNGLClientException(
					"Number of partitions can be greater than 0 for user specified partitioning or equal to -1 for system decided partitioning");
		this.numPartitions = numPartitions;
	}

	/**
	 * Sample size to use for seeding labelled data We dont want to run over all
	 * the data, as we want a quick way to seed some labeled data which we can
	 * manually edit
	 * 
	 * @return sample percent as a float between 0 and 1
	 */

	public float getLabelDataSampleSize() {
		return labelDataSampleSize;
	}

	/**
	 * Set the fraction of data to be used from complete data set to be used for
	 * seeding the labelled data Labelling is costly and we want a fast
	 * approximate way of looking at a small sample of the records and
	 * identifying expected matches and non matches
	 * 
	 * @param labelDataSampleSize
	 *            - float between 0 and 1 denoting portion of dataset to use in
	 *            generating seed samples
	 * @throws SiNGLClientException 
	 */
	public void setLabelDataSampleSize(float labelDataSampleSize) throws SiNGLClientException {
		if (labelDataSampleSize > 1 || labelDataSampleSize < 0)
			throw new SiNGLClientException("Label Data Sample Size should be between 0 and 1");
		this.labelDataSampleSize = labelDataSampleSize;
	}

	/**
	 * get the field definitions associated with this client
	 * 
	 * @return list of field definitions
	 */
	public List<FieldDefinition> getFieldDefinition() {
		return fieldDefinition;
	}

	/**
	 * Set the field definitions consisting of match field indices, types and
	 * classes
	 * 
	 * @see FieldDefinition
	 * @param fieldDefinition
	 *            list of fields
	 * @throws SiNGLClientException 
	 */
	public void setFieldDefinition(List<FieldDefinition> fieldDefinition) throws SiNGLClientException {
		/*if (fieldDefinition == null || fieldDefinition.size() ==0) 
			throw new ZinggClientException("Missing or incorrect field definitions");
		*///Collections.sort(fieldDefinition);
		this.fieldDefinition = fieldDefinition;
	}

	/**
	 * Return the path to the positive labeled samples file
	 * 
	 * @return path to labeled positive sample file
	 */
	public Pipe[] getTrainingSamples() {
		return trainingSamples;
	}

	/**
	 * Set the path to the positive training sample file
	 * 
	 * @param positiveTrainingSamples
	 *            path of the matching (positive)labeled sample file
	 * @throws SiNGLClientException 
	 */
	@JsonSetter
	public void setTrainingSamples(Pipe[] trainingSamples) throws SiNGLClientException {
		//checkNullBlankEmpty(positiveTrainingSamples, "positive training samples");
		this.trainingSamples = trainingSamples;
	}


	/**
	 * Location for internal SiNGL use.
	 * 
	 * @return the path for internal SiNGL usage
	 
	public Pipe[] getSiNGLInternal() {
		return siNGLInternal;
	}

	/**
	 * Set the location for SiNGL to save its internal computations and
	 * models. Please set it to a place where the program has write access.
	 * 
	 * @param siNGLDir
	 *            path to the SiNGL directory
	 
	public void setSiNGLDirInternal(Pipe[] siNGLDir) {
		this.siNGLInternal = siNGLDir;
	}
	*/
	
	

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	/**
	 * Get the output directory where the match output will be saved
	 * 
	 * @return output directory path of the result
	 */
	public Pipe[] getOutput() {
		return output;
	}

	/**
	 * Set the output directory where the match result will be saved
	 * 
	 * @param outputDir
	 *            where the match result is saved
	 * @throws SiNGLClientException 
	 */
	public void setOutput(Pipe[] outputDir) throws SiNGLClientException {
		//checkNullBlankEmpty(outputDir, " path for saving results");
		this.output = outputDir;
	}

	/**
	 * Get the location of the data file over which the match will be run
	 * 
	 * @return path of data file to be matched
	 */
	public Pipe[] getData() {
		return this.data;
	}

	/**
	 * Set the file path of the file to be matched.
	 * 
	 * @param dataFile
	 *            - full file path
	 *            /home/singl/path/to/my/file/to/be/matched.csv
	 * @throws SiNGLClientException 
	 */
	public void setData(Pipe[] dataFile) throws SiNGLClientException {
		checkNullBlankEmpty(dataFile, "file to be matched");
		this.data = dataFile;
	}

		
	/**
	 * Checks if the given arguments are correct or not
	 * @param args
	 * @throws SiNGLClientException
	 */
	public static void checkIsValid(Arguments args) throws SiNGLClientException {
		Arguments arg = new Arguments();
		arg.setTrainingSamples(args.getTrainingSamples());
		arg.setData(args.getData());
		arg.setNumPartitions(args.getNumPartitions());
		arg.setFieldDefinition(args.getFieldDefinition());
	}
	
	public static void checkIsValidForOthers(Arguments args) throws SiNGLClientException {
		Arguments arg = new Arguments();
		arg.setData(args.getData());
		arg.setNumPartitions(args.getNumPartitions());
	}
	
	
	public static void checkIsValidForLabelling(Arguments args) throws SiNGLClientException {
		Arguments arg = new Arguments();
		//arg.setPositiveTrainingSamples(args.getPositiveTrainingSamples());
		//arg.setNegativeTrainingSamples(args.getNegativeTrainingSamples());
		arg.setData(args.getData());
		arg.setNumPartitions(args.getNumPartitions());
		arg.setFieldDefinition(args.getFieldDefinition());
	}
	
	public static void checkNullBlankEmpty(String field, String fieldName) throws SiNGLClientException {
		if (field == null || field.trim().length() == 0) {
			throw new SiNGLClientException("Missing value for " + fieldName + ". Trying to set " + field);
		}
	}
	
	public static void checkNullBlankEmpty(Pipe[] field, String fieldName) throws SiNGLClientException {
		if (field == null || field.length == 0) {
			throw new SiNGLClientException("Missing value for " + fieldName + ". Trying to set " + field);
		}
	}
	
	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
				true);
		//mapper.configure(JsonParser.Feature.FAIL_ON_EMPTY_BEANS, true)
		try {
			StringWriter writer = new StringWriter();
			return mapper.writeValueAsString(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Location for internal SiNGL use.
	 * 
	 * @return the path for internal SiNGL usage
	 */
	public String getSiNGLDir() {
		return siNGLDir;
	}

	/**
	 * Set the location for SiNGL to save its internal computations and
	 * models. Please set it to a place where the program has write access.
	 * 
	 * @param siNGLDir
	 *            path to the SiNGL directory
	 */
	public void setSiNGLDir(String siNGLDir) {
		this.siNGLDir = siNGLDir;
	}

	
	/**
	 * Location for internal SiNGL use.
	 * 
	 * @return the path for internal SiNGL usage
	 */
	@JsonIgnore
	public String getSiNGLBaseModelDir() {
		return siNGLDir + "/" + modelId + "/model";
	}

	@JsonIgnore
	public String getSiNGLDocDir() {
		return getSiNGLBaseModelDir();
	}

	@JsonIgnore
	public String getSiNGLDocFile() {
		return siNGLDir + "/" + modelId + "/model.html";
	}


	/**
	 * Location for internal SiNGL use.
	 * 
	 * @return the path for internal SiNGL usage
	 */
	@JsonIgnore
	public String getSiNGLBaseTrainingDataDir() {
		return siNGLDir + "/" + modelId + "/trainingData/";
	}



	/**
	 * Location for internal SiNGL use.
	 * 
	 * @return the path for internal SiNGL usage
	 */
	@JsonIgnore
	public String getSiNGLTrainingDataUnmarkedDir() {
		return this.getSiNGLBaseTrainingDataDir() + "/unmarked/";
	}

	/**
	 * Location for internal SiNGL use.
	 * 
	 * @return the path for internal SiNGL usage
	 */
	@JsonIgnore
	public String getSiNGLTrainingDataMarkedDir() {
		return this.getSiNGLBaseTrainingDataDir() + "/marked/";
	}
	
	/**
	 * Location for internal SiNGL use.
	 * 
	 * @return the path for internal SiNGL usage
	 */
	@JsonIgnore
	public String getSiNGLPreprocessedDataDir() {
		return siNGLDir + "/preprocess";
	}
	
	/**
	 * This is an internal block file location Not to be used directly by the
	 * client
	 * 
	 * @return the blockFile
	 */
	@JsonIgnore
	public String getBlockFile() {
		return getSiNGLBaseModelDir() + "/block/singl.block";
	}
	
	/**
	 * This is the internal model location Not to be used by the client
	 * 
	 * @return model path
	 */
	@JsonIgnore
	public String getModel() {
		return getSiNGLBaseModelDir() + "/classifier/best.model";
	}



	public int getJobId() {
		return jobId;
	}



	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public boolean getCollectMetrics() {
		return collectMetrics;
	}

	public void setCollectMetrics(boolean collectMetrics) {
		this.collectMetrics = collectMetrics;
	}

	public boolean getShowConcise() {
		return showConcise;
	}

	public void setShowConcise(boolean showConcise) {
		this.showConcise = showConcise;
	}
	
	public String[] getPipeNames() {
		Pipe[] input = this.getData();
		String[] sourceNames = new String[input.length];
		int i = 0;
		for (Pipe p: input) {
			sourceNames[i++] = p.getName();
		}
		return sourceNames;
	}

}
