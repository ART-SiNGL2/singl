package singl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import singl.block.Canopy;
import singl.block.Tree;
import singl.client.SiNGLClientException;
import singl.client.SiNGLOptions;
import singl.client.util.ColName;
import singl.client.util.ColValues;
import singl.model.Model;
import singl.util.Analytics;
import singl.util.BlockingTreeUtil;
import singl.util.DSUtil;
import singl.util.Metric;
import singl.util.ModelUtil;
import singl.util.PipeUtil;

public class Trainer extends SiNGLBase{

	protected static String name = "singl.Trainer";
	public static final Log LOG = LogFactory.getLog(Trainer.class);    

    public Trainer() {
        setSiNGLOptions(SiNGLOptions.TRAIN);
    }

	public void execute() throws SiNGLClientException {
        try {
			LOG.info("Reading inputs for training phase ...");
			LOG.info("Initializing learning similarity rules");
			
			Dataset<Row> positives = null;
			Dataset<Row> negatives = null;
			Dataset<Row> tra = DSUtil.getTraining(spark, args);
			tra = DSUtil.joinWithItself(tra, ColName.CLUSTER_COLUMN, true);
			tra = tra.cache();
			positives = tra.filter(tra.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_MATCH));
			negatives = tra.filter(tra.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_NOT_A_MATCH));
			LOG.warn("Training on positive pairs - " + positives.count());
			LOG.warn("Training on negative pairs - " + negatives.count());
				
			Dataset<Row> testData = PipeUtil.read(spark, true, args.getNumPartitions(), false, args.getData());
			Tree<Canopy> blockingTree = BlockingTreeUtil.createBlockingTreeFromSample(testData,  positives, 0.5,
					-1, args, hashFunctions);
			if (blockingTree == null || blockingTree.getSubTrees() == null) {
				LOG.warn("Seems like no indexing rules have been learnt");
			}
			BlockingTreeUtil.writeBlockingTree(spark, ctx, blockingTree, args);
			LOG.info("Learnt indexing rules and saved output at " + args.getSiNGLDir());
			// model
			Model model = ModelUtil.createModel(positives, negatives, new Model(this.featurers), spark);
			model.save(args.getModel());
			LOG.info("Learnt similarity rules and saved output at " + args.getSiNGLDir());
			Analytics.track(Metric.TRAINING_MATCHES, Metric.approxCount(positives), args.getCollectMetrics());
			Analytics.track(Metric.TRAINING_NONMATCHES, Metric.approxCount(negatives), args.getCollectMetrics());
			LOG.info("Finished Learning phase");			
		} catch (Exception e) {
			e.printStackTrace();
			throw new SiNGLClientException(e.getMessage());
		}
    }

		    
}
