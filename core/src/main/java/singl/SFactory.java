package singl;

import java.util.HashMap;

import singl.client.ISiNGL;
import singl.client.ISiNGLFactory;
import singl.client.SiNGLOptions;

public class SFactory implements ISiNGLFactory{

    public SFactory() {}

    public static HashMap<SiNGLOptions, String> singls = new  HashMap<SiNGLOptions, String>();

    static {
        singls.put(SiNGLOptions.TRAIN, Trainer.name);
        singls.put(SiNGLOptions.FIND_TRAINING_DATA, TrainingDataFinder.name);
        singls.put(SiNGLOptions.LABEL, Labeller.name);
        singls.put(SiNGLOptions.MATCH, Matcher.name);
        singls.put(SiNGLOptions.TRAIN_MATCH, TrainMatcher.name);
        singls.put(SiNGLOptions.LINK, Linker.name);
        singls.put(SiNGLOptions.GENERATE_DOCS, Documenter.name);
        singls.put(SiNGLOptions.UPDATE_LABEL, LabelUpdater.name);
        singls.put(SiNGLOptions.FIND_AND_LABEL, FindAndLabeller.name);
    }

    public ISiNGL get(SiNGLOptions z) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return (ISiNGL) Class.forName(singls.get(z)).newInstance();
    }

   
    
}
