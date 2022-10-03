package singl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import singl.client.Arguments;
import singl.client.FieldDefinition;
import singl.client.ISiNGL;
import singl.client.MatchType;
import singl.client.SiNGLClientException;
import singl.client.SiNGLOptions;
import singl.client.util.ListMap;
import singl.feature.Feature;
import singl.feature.FeatureFactory;
import singl.hash.HashFunction;
import singl.util.Analytics;
import singl.util.DSUtil;
import singl.util.HashUtil;
import singl.util.Metric;
import singl.util.PipeUtil;

public abstract class SiNGLBase implements Serializable, ISiNGL {

    protected Arguments args;
	
    protected JavaSparkContext ctx;
	protected SparkSession spark;
    protected static String name;
    protected SiNGLOptions siNGLOptions;
    protected ListMap<DataType, HashFunction> hashFunctions;
	protected Map<FieldDefinition, Feature> featurers;
    protected long startTime;
	public static final String hashFunctionFile = "hashFunctions.json";

    public static final Log LOG = LogFactory.getLog(SiNGLBase.class);

    @Override
    public void init(Arguments args, String license)
        throws SiNGLClientException {
        startTime = System.currentTimeMillis();
        this.args = args;
        try{
            spark = SparkSession
                .builder()
                .appName("SiNGL"+args.getJobId())
                .getOrCreate();
            ctx = new JavaSparkContext(spark.sparkContext());
            JavaSparkContext.jarOfClass(ISiNGL.class);
            LOG.debug("Context " + ctx.toString());
            initHashFns();
            loadFeatures();
            ctx.setCheckpointDir("/tmp/checkpoint");	
        }
        catch(Throwable e) {
            if (LOG.isDebugEnabled()) e.printStackTrace();
            throw new SiNGLClientException(e.getMessage());
        }
    }


    @Override
    public void cleanup() throws SiNGLClientException {
        if (ctx != null) ctx.stop();
    }

    void initHashFns() throws SiNGLClientException {
		try {
			//functions = Util.getFunctionList(this.functionFile);
			hashFunctions = HashUtil.getHashFunctionList(this.hashFunctionFile, spark);
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) e.printStackTrace();
			throw new SiNGLClientException("Unable to initialize base functions");
		}		
	}

    public void loadFeatures() throws SiNGLClientException {
		try{
		LOG.info("Start reading internal configurations and functions");
		if (args.getFieldDefinition() != null) {
			featurers = new HashMap<FieldDefinition, Feature>();
			for (FieldDefinition def : args.getFieldDefinition()) {
				if (! (def.getMatchType() == null || def.getMatchType().equals(MatchType.DONT_USE))) {
					Feature fea = (Feature) FeatureFactory.get(def.getDataType());
					fea.init(def);
					featurers.put(def, fea);			
				}
			}
			LOG.info("Finished reading internal configurations and functions");
			}
		}
		catch(Throwable t) {
			LOG.warn("Unable to initialize internal configurations and functions");
			if (LOG.isDebugEnabled()) t.printStackTrace();
			throw new SiNGLClientException("Unable to initialize internal configurations and functions");
		}
	}

    public void copyContext(SiNGLBase b) {
            this.args = b.args;
            this.ctx = b.ctx;
            this.spark = b.spark;
            this.featurers = b.featurers;
            this.hashFunctions = b.hashFunctions;
    }

	public void postMetrics() {
        boolean collectMetrics = args.getCollectMetrics();
        Analytics.track(Metric.EXEC_TIME, (System.currentTimeMillis() - startTime) / 1000, collectMetrics);
		Analytics.track(Metric.TOTAL_FIELDS_COUNT, args.getFieldDefinition().size(), collectMetrics);
        Analytics.track(Metric.MATCH_FIELDS_COUNT, DSUtil.getFieldDefinitionFiltered(args, MatchType.DONT_USE).size(),
                collectMetrics);
		Analytics.track(Metric.DATA_FORMAT, PipeUtil.getPipesAsString(args.getData()), collectMetrics);
		Analytics.track(Metric.OUTPUT_FORMAT, PipeUtil.getPipesAsString(args.getOutput()), collectMetrics);

		Analytics.postEvent(siNGLOptions.getValue(), collectMetrics);
	}

    public Arguments getArgs() {
        return this.args;
    }

    public void setArgs(Arguments args) {
        this.args = args;
    }

    public ListMap<DataType,HashFunction> getHashFunctions() {
        return this.hashFunctions;
    }

    public void setHashFunctions(ListMap<DataType,HashFunction> hashFunctions) {
        this.hashFunctions = hashFunctions;
    }

    public Map<FieldDefinition,Feature> getFeaturers() {
        return this.featurers;
    }

    public void setFeaturers(Map<FieldDefinition,Feature> featurers) {
        this.featurers = featurers;
    }

    public JavaSparkContext getCtx() {
        return this.ctx;
    }

    public void setCtx(JavaSparkContext ctx) {
        this.ctx = ctx;
    }

    public SparkSession getSpark() {
        return this.spark;
    }

    public void setSpark(SparkSession spark) {
        this.spark = spark;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setSiNGLOptions(SiNGLOptions siNGLOptions) {
        this.siNGLOptions = siNGLOptions;
    }

	public String getName() {
        return name;
    }

    public SiNGLOptions getSiNGLOptions() {
        return siNGLOptions;
    }



  
 }