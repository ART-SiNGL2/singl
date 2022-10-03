package singl.client;

import java.util.Arrays;

import singl.client.util.Util;

public enum SiNGLOptions {
    
    TRAIN("train"), 
    MATCH("match"), 
    TRAIN_MATCH("trainMatch"), 
    FIND_TRAINING_DATA("findTrainingData"), 
    LABEL("label"),
    LINK("link"),
    GENERATE_DOCS("generateDocs"),
    UPDATE_LABEL("updateLabel"),
    FIND_AND_LABEL("findAndLabel");

    private String value;

    SiNGLOptions(String s) {
        this.value = s;
    }

    public static String[] getAllSiNGLOptions() {
        SiNGLOptions[] zo = SiNGLOptions.values();
        int i = 0;
        String[] s = new String[zo.length];
        for (SiNGLOptions z: zo) {
            s[i++] = z.getValue();
        }
        return s;
    }

    public String getValue() { 
        return value;
    }

    public static final SiNGLOptions getByValue(String value){
        for (SiNGLOptions zo: SiNGLOptions.values()) {
            if (zo.value.equals(value)) return zo;
        }
        return null;
    }

	public static void verifyPhase(String phase) throws SiNGLClientException {
		if (getByValue(phase) == null) {	
			String message = "'" + phase + "' is not a valid phase. "
			               + "Valid phases are: " + Util.join(getAllSiNGLOptions(), "|");
			throw new SiNGLClientException(message);
		}
	}
}