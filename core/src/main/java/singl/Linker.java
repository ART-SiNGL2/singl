package singl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.RelationalGroupedDataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

import scala.collection.JavaConverters;
import singl.block.Block;
import singl.block.Canopy;
import singl.block.Tree;
import singl.client.SiNGLClientException;
import singl.client.SiNGLOptions;
import singl.client.util.ColName;
import singl.client.util.ColValues;
import singl.client.util.Util;
import singl.model.Model;
import singl.scala.DFUtil;
import singl.scala.TypeTags;
import singl.util.BlockingTreeUtil;
import singl.util.DSUtil;
import singl.util.GraphUtil;
import singl.util.ModelUtil;
import singl.util.PipeUtil;

public class Linker extends Matcher {

	protected static String name = "singl.Linker";
	public static final Log LOG = LogFactory.getLog(Linker.class);

	public Linker() {
		setSiNGLOptions(SiNGLOptions.LINK);
	}

	protected Dataset<Row> getBlocks(Dataset<Row> blocked, Dataset<Row> bAll) throws Exception{
		return DSUtil.joinWithItselfSourceSensitive(blocked, ColName.HASH_COL, args).cache();
	}

	protected Dataset<Row> selectColsFromBlocked(Dataset<Row> blocked) {
		return blocked;
	}

	public void writeOutput(Dataset<Row> blocked, Dataset<Row> dupes) {
		try {
			// input dupes are pairs
			/// pick ones according to the threshold by user
			Dataset<Row> dupesActual = getDupesActualForGraph(dupes);

			// all clusters consolidated in one place
			if (args.getOutput() != null) {

				// input dupes are pairs
				//dupesActual = DFUtil.addClusterRowNumber(dupesActual, spark);
				dupesActual = Util.addUniqueCol(dupesActual, ColName.ID_COL);
				Dataset<Row> dupes2 = DSUtil.alignLinked(dupesActual, args);
				LOG.debug("uncertain output schema is " + dupes2.schema());
				PipeUtil.write(dupes2, args, ctx, args.getOutput());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Dataset<Row> getDupesActualForGraph(Dataset<Row> dupes) {
		Dataset<Row> dupesActual = dupes
				.filter(dupes.col(ColName.PREDICTION_COL).equalTo(ColValues.IS_MATCH_PREDICTION));
		return dupesActual;
	}

}
