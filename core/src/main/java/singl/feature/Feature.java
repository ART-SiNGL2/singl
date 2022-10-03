package singl.feature;

import java.io.Serializable;
import java.util.List;

import singl.client.FieldDefinition;
import singl.client.MatchType;
import singl.similarity.function.BaseSimilarityFunction;

public interface Feature<T> extends Serializable {

	void setFieldDefinition(FieldDefinition f);
	
	FieldDefinition getFieldDefinition();

	MatchType getMatchType();

	BaseSimilarityFunction<T> getSimFunction(int i);
	
	List<BaseSimilarityFunction<T>> getSimFunctions();

	void init(FieldDefinition newParam);
	
	//String[] getCols();

	void addSimFunction(BaseSimilarityFunction<T> b);
}
