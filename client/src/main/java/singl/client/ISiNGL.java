package singl.client;

public interface ISiNGL {

	public void init(Arguments args, String license)
			throws SiNGLClientException;

	public void execute() throws SiNGLClientException;

	public void cleanup() throws SiNGLClientException;

	public SiNGLOptions getSiNGLOptions();	

	public String getName();

	public void postMetrics();

}
