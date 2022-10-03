package singl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import freemarker.ext.rhino.RhinoWrapper;
import freemarker.template.*;
import singl.client.SiNGLClientException;
import singl.client.SiNGLOptions;
import singl.client.pipe.Pipe;
import singl.client.util.ColName;
import singl.client.util.ColValues;
import singl.util.DSUtil;
import singl.util.PipeUtil;
import singl.util.RowAdapter;
import singl.util.RowWrapper;

import java.util.*;
import java.io.*;

public class Documenter extends SiNGLBase {

	protected static String name = "singl.Documenter";
	public static final Log LOG = LogFactory.getLog(Documenter.class);

	public Documenter() {
		setSiNGLOptions(SiNGLOptions.GENERATE_DOCS);
	}

	public void execute() throws SiNGLClientException {
		try {
			LOG.info("Document generation in progress");
			Dataset<Row> markedRecords = PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataMarkedPipe(args));
			markedRecords = markedRecords.cache();
			//List<Column> displayCols = DSUtil.getFieldDefColumns(markedRecords, args, false);
			List<Row> clusterIDs = markedRecords.select(ColName.CLUSTER_COLUMN).distinct().collectAsList();
			int totalPairs = clusterIDs.size();
			/* Create a data-model */
			Map<String, Object> root = new HashMap<String, Object>();
			root.put("modelId", args.getModelId());
			root.put("clusters", markedRecords.collectAsList());
			root.put("numColumns", markedRecords.columns().length);
			root.put("columns", markedRecords.columns());
			root.put("fieldDefinitionCount", args.getFieldDefinition().size());
			buildAndWriteHTML(root);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SiNGLClientException(e.getMessage());
		}
	}

	public void buildAndWriteHTML(Map<String, Object> root) throws Exception {

        /* ------------------------------------------------------------------------ */
        /* You should do this ONLY ONCE in the whole application life-cycle:        */

        /* Create and adjust the configuration singleton */
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
		cfg.setClassForTemplateLoading(this.getClass(), "/");
       // cfg.setDirectoryForTemplateLoading(new File("/where/you/store/templates"));
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
		cfg.setObjectWrapper(new RowWrapper(cfg.getIncompatibleImprovements()));

        /* ------------------------------------------------------------------------ */
        /* You usually do these for MULTIPLE TIMES in the application life-cycle:   */

        
       
        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate("model.ftlh");

        /* Merge data-model with template */
       // Writer out = new OutputStreamWriter(System.out);
		Writer file = new FileWriter (new File(args.getSiNGLDocFile()));
		//StringWriter writer = new StringWriter();
        temp.process(root, file);
        // Note: Depending on what `out` is, you may need to call `out.close()`.
        // This is usually the case for file output, but not for servlet output.
		//file.flush();

		//List<String> textList = Collections.singletonList(writer.toString());
		
		//Dataset<Row> data = spark.createDataset(textList, Encoders.STRING()).toDF();

		//PipeUtil.write(data, args, ctx, PipeUtil.getModelDocumentationPipe(args));
        file.close();
		//LOG.warn("written documentation at " + args.getZinggDocFile());
    }

	
	
	
}
