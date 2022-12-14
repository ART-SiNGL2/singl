package singl.util;

import org.apache.spark.sql.functions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.storage.StorageLevel;

import singl.client.SiNGLClientException;
import singl.client.util.ColName;
import singl.client.util.ColValues;
import singl.model.Model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.sql.SparkSession;

public class ModelUtil {

    public static final Log LOG = LogFactory.getLog(ModelUtil.class);

	public static Model createModel(Dataset<Row> positives,
        Dataset<Row> negatives, Model model, SparkSession spark) throws Exception, SiNGLClientException {
        LOG.info("Learning similarity rules");
        Dataset<Row> posLabeledPointsWithLabel = positives.withColumn(ColName.MATCH_FLAG_COL, functions.lit(ColValues.MATCH_TYPE_MATCH));
        posLabeledPointsWithLabel = posLabeledPointsWithLabel.persist(StorageLevel.MEMORY_ONLY());
        posLabeledPointsWithLabel = posLabeledPointsWithLabel.drop(ColName.PREDICTION_COL);
        Dataset<Row> negLabeledPointsWithLabel = negatives.withColumn(ColName.MATCH_FLAG_COL, functions.lit(ColValues.MATCH_TYPE_NOT_A_MATCH));
        negLabeledPointsWithLabel = negLabeledPointsWithLabel.persist(StorageLevel.MEMORY_ONLY());
        negLabeledPointsWithLabel = negLabeledPointsWithLabel.drop(ColName.PREDICTION_COL);
        if (LOG.isDebugEnabled()) {
            LOG.debug(" +,-,Total labeled data "
                    + posLabeledPointsWithLabel.count() + ", "
                    + negLabeledPointsWithLabel.count());
        }
        model.register(spark);
        model.fit(posLabeledPointsWithLabel, negLabeledPointsWithLabel);
        return model;
    }



    
}
