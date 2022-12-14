package singl.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import singl.block.Canopy;


public class FirstChars extends HashFunction implements UDF1<String, String>{
	
	public static final Log LOG = LogFactory.getLog(FirstChars.class);

	int endIndex;
	
	public FirstChars(int endIndex) {
		super("first" + endIndex + "Chars", DataTypes.StringType, DataTypes.StringType);
		this.endIndex = endIndex;
	}
	
	 @Override
	 public String call(String field) {
		 
		 String r = null;
			if (field == null ) {
				r = field;
			}
			else{
				field = field.trim().toLowerCase();
				if (field.length() <= (endIndex)) {
			
				r = field; 
			}
			else {
				r = field.trim().substring(0, endIndex);
			}
			}
			LOG.debug("Applying " + this.name + " on " + field + " and returning " + r);
			return r;
	 }

	public Object apply(Row ds, String column) {
		 return call((String) ds.getAs(column));
	}

	

}
