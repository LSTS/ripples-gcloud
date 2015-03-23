package pt.lsts.ripples.model.log;

import java.io.Serializable;

public class ActionItem implements Serializable, Comparable<ActionItem> {
	private static final long serialVersionUID = -1733587853611552588L;
	public String module;
	public String text;	
	
	@Override
	public int compareTo(ActionItem o) {
		return (module+";"+text).compareTo(o.module+";"+o.text);
	}
}
