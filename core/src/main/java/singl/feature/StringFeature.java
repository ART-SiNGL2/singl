package singl.feature;

import singl.client.FieldDefinition;
import singl.client.MatchType;
import singl.similarity.function.AJaroWinklerFunction;
import singl.similarity.function.AffineGapSimilarityFunction;
import singl.similarity.function.JaccSimFunction;
import singl.similarity.function.JaroWinklerFunction;
import singl.similarity.function.NumbersJaccardFunction;
import singl.similarity.function.ProductCodeFunction;
import singl.similarity.function.SameFirstWordFunction;
import singl.similarity.function.StringSimilarityFunction;

public class StringFeature extends BaseFeature<String> {

	public StringFeature() {

	}

	public void init(FieldDefinition f) {
		setFieldDefinition(f);
		// based on stat, evaluate which function(s) to use
		// if long string, cosine
		// if short string, affine gap
		// if short string but inverted, like fname lname where ordering is not
		// important
		// then do cosine or something
		if (f.getMatchType() == MatchType.FUZZY) {
			addSimFunction(new AffineGapSimilarityFunction());
			addSimFunction(new JaroWinklerFunction());
			//addSimFunction(new JaccSimFunction());
			// addSimFunction(new First3CharactersFunction(3));
			// addSimFunction(new First3CharactersFunction(4));
			//addSimFunction(new BigramJaccSimFn());
		} /*else if (f.getMatchType() == MatchType.FUZZY_LAST) {
			addSimFunction(new AffineGapSimilarityFunction());
			addSimFunction(new JaroWinklerFunction());
			//addSimFunction(new SameFirstWordFunction());
			//addSimFunction(new Last3CharactersFunction(3));
			// addSimFunction(new Last3CharactersFunction(4));
		} */else if (f.getMatchType() == MatchType.FUZZY_GARBLED) {
			addSimFunction(new AffineGapSimilarityFunction());
			addSimFunction(new JaroWinklerFunction());
			addSimFunction(new JaccSimFunction());
		} else if (f.getMatchType() == MatchType.TEXT) {
			addSimFunction(new JaccSimFunction());
			//change for azalead
			//addSimFunction(new AffineGapSimilarityFunction());
			//addSimFunction(new JaroWinklerFunction());
			//addSimFunction(new JaccSimFunction());
			//addSimFunction(new SetMembershipFunction());
			// addSimFunction(new First3CharactersFunction(3));
			// addSimFunction(new First3CharactersFunction(4));
			// simFunctions.add(new JaroWinklerFunction());
		} else if (f.getMatchType() == MatchType.ALPHANUMERIC) {
			//addSimFunction(new AffineGapSimilarityFunction());
			addSimFunction(new JaroWinklerFunction());
			addSimFunction(new NumbersJaccardFunction());
			//addSimFunction(new ProductCodeFunction());
			// simFunctions.add(new JaroWinklerFunction());
		} else if (f.getMatchType() == MatchType.EXACT) {
			addSimFunction(new StringSimilarityFunction());
		} else if (f.getMatchType() == MatchType.ALPHANUMERIC_WITH_UNITS) {
			addSimFunction(new AffineGapSimilarityFunction());
			addSimFunction(new JaroWinklerFunction());
			addSimFunction(new ProductCodeFunction());
			//addSimFunction(new NumbersJaccardFunction());
			// addSimFunction(new First3CharactersFunction(3));
			// addSimFunction(new First3CharactersFunction(4));
			//addSimFunction(new BigramJaccSimFn());
		}
	}

}
