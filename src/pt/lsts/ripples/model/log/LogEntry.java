package pt.lsts.ripples.model.log;

import java.io.Serializable;
import java.util.ArrayList;

public class LogEntry implements Serializable {
	
	private static final long serialVersionUID = -6818557926512515218L;
	public static enum TYPE {
		REGULAR,
		WARNING		
	}
	
	public long timestamp = System.currentTimeMillis();
	public ArrayList<String> tags = new ArrayList<String>();
	
	public String author = "";
	public String text = "";
	public String dataUrl;
}
