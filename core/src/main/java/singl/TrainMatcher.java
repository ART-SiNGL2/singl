package singl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;

import scala.collection.JavaConverters;
import singl.block.Block;
import singl.block.Canopy;
import singl.block.Tree;
import singl.client.Arguments;
import singl.client.SiNGLClientException;
import singl.client.SiNGLOptions;
import singl.client.util.ColName;
import singl.client.util.Util;
import singl.model.Model;
import singl.util.BlockingTreeUtil;
import singl.util.DSUtil;
import singl.util.GraphUtil;
import singl.util.ModelUtil;
import singl.util.PipeUtil;

public class TrainMatcher extends Matcher{

	protected static String name = "singl.TrainMatcher";
	public static final Log LOG = LogFactory.getLog(TrainMatcher.class); 
	
	private Trainer trainer;

    public TrainMatcher() {
        setSiNGLOptions(SiNGLOptions.TRAIN_MATCH);
		trainer = new Trainer();
    }

	@Override
	public void init(Arguments args, String license)
        throws SiNGLClientException {
			super.init(args, license);
			trainer.copyContext(this);
	}

	@Override
    public void execute() throws SiNGLClientException {
		trainer.execute();
		super.execute();
	}


	
	    
}
