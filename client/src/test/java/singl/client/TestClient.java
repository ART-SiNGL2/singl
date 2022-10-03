package singl.client;

import static org.junit.Assert.fail;

import org.junit.Test;

import singl.client.SiNGLClientException;
import singl.client.SiNGLOptions;

public class TestClient {

	@Test
	public void testValidPhase() {
		String phase = "train";
		try {
			SiNGLOptions.verifyPhase(phase);
		} catch (SiNGLClientException e1) {
			fail("No exception was expected as it is a valid phase: " + phase);
		}
	}

	@Test
	public void testInvalidPhase() {
		String phase = "tain";
		try {
			SiNGLOptions.verifyPhase(phase);
			fail("An exception should have been thrown for an invalid phase");
		} catch (SiNGLClientException e1) {
			System.out.println("Expected exception as it is an invalid phase: " + phase);
		}
	}
}