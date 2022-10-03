package singl.feature;

import singl.client.FieldDefinition;
import singl.client.MatchType;
import singl.similarity.function.DoubleSimilarityFunction;


public class DoubleFeature extends BaseFeature<Double> {

	public DoubleFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType() == MatchType.NUMERIC) {
			addSimFunction(new DoubleSimilarityFunction());
		} /*else {
			addSimFunction(new ObjectSimilarityFunction());
		}*/
	}

}
