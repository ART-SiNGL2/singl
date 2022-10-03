package singl.stat;

import java.io.Serializable;

import singl.client.MatchType;

public interface Stat<E> extends Serializable {
	MatchType getFieldType();

	public void setFieldType(MatchType f);

}
