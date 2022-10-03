package singl.client;

import java.io.Serializable;

/**
 * Base class for all SiNGL Exceptions
 * 
 * @author sgoyal
 *
 */

public class SiNGLClientException extends Throwable implements Serializable {

	public String message;

	public SiNGLClientException(String m) {
		super(m);
		this.message = m;
	}

}
