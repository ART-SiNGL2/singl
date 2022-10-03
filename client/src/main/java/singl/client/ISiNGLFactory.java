package singl.client;

public interface ISiNGLFactory {

    public ISiNGL get(SiNGLOptions z) throws InstantiationException, IllegalAccessException, ClassNotFoundException;
    
}
