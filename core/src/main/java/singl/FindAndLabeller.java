package singl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import singl.client.Arguments;
import singl.client.SiNGLClientException;
import singl.client.SiNGLOptions;

public class FindAndLabeller extends Labeller {
	protected static String name = "singl.FindAndLabeller";
	public static final Log LOG = LogFactory.getLog(FindAndLabeller.class);

	private TrainingDataFinder finder;

	public FindAndLabeller() {
		setSiNGLOptions(SiNGLOptions.FIND_AND_LABEL);
		finder = new TrainingDataFinder();
	}

	@Override
	public void init(Arguments args, String license)
			throws SiNGLClientException {
		super.init(args, license);
		finder.copyContext(this);
	}

	@Override
	public void execute() throws SiNGLClientException {
		finder.execute();
		super.execute();
	}
}
