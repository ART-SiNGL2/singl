package singl.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class LastChars extends HashFunction implements UDF1<String, String>{
	int numChars;
	public LastChars(int endIndex) {
		super("last" + endIndex + "Chars", DataTypes.StringType, DataTypes.StringType, true);
		this.numChars = endIndex;
	}

	

		
		 @Override
		 public String call(String field) {
				String r = null;
				if (field == null ) {
					r = field;
				}
				else {
					field = field.trim().toLowerCase();
					r= field.trim().toLowerCase().substring(Math.max(field.length() - numChars, 0));
				}
				return r;
			 }

		 public Object apply(Row ds, String column) {
			 return call((String) ds.getAs(column));
		 }

}
