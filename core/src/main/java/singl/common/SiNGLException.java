package singl.common;

import java.io.Serializable;

public class SiNGLException extends RuntimeException implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Constructor SiNGLException creates a new SiNGLException instance. */
	public SiNGLException() {
	}

	/**
	 * Constructor SiNGLException creates a new SiNGLException instance.
	 *
	 * @param string
	 *            of type String
	 */
	public SiNGLException(String string) {
		super(string);
	}

	/**
	 * Constructor SiNGLException creates a new SiNGLException instance.
	 *
	 * @param string
	 *            of type String
	 * @param throwable
	 *            of type Throwable
	 */
	public SiNGLException(String string, Throwable throwable) {
		super(string, throwable);
	}

	/**
	 * Constructor SiNGLException creates a new SiNGLException instance.
	 *
	 * @param throwable
	 *            of type Throwable
	 */
	public SiNGLException(Throwable throwable) {
		super(throwable);
	}
}
