package singl.feature;

import singl.client.FieldDefinition;
import singl.client.MatchType;
import singl.similarity.function.IntegerSimilarityFunction;
public class IntFeature extends BaseFeature<Integer> {

	public IntFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType() == MatchType.NUMERIC) {
			addSimFunction(new IntegerSimilarityFunction());
		} /*else {
			addSimFunction(new ObjectSimilarityFunction());
		}*/
	}

}
