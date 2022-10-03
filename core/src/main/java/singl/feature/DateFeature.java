package singl.feature;

import java.util.Date;

import singl.client.FieldDefinition;
import singl.client.MatchType;
import singl.similarity.function.DateSimilarityFunction;
import singl.similarity.function.StringSimilarityFunction;


public class DateFeature extends BaseFeature<Date> {

	public DateFeature() {

	}

	public void init(FieldDefinition f) {
		setFieldDefinition(f);
		// based on stat, evaluate which function(s) to use
		// if long string, cosine
		// if short string, affine gap
		// if short string but inverted, like fname lname where ordering is not
		// important
		// then do cosine or something
		/*if (f == FieldType.WORD) {
			addSimFunction(new AffineGapSimilarityFunction());
			addSimFunction(new JaroWinklerFunction());			
		}
		else*/ if (f.getMatchType() == MatchType.NUMERIC) {
			addSimFunction(new DateSimilarityFunction());
		} 
		/*else if (f == MatchType.EXACT) {
			addSimFunction(new StringSimilarityFunction());
		}*/
	}

}
