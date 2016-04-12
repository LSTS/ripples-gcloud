package pt.lsts.ripples.model;

import com.googlecode.objectify.annotation.Subclass;

@Subclass(index=true)
public class EventSample extends HistoricDatum {
	
	public String text;
	public boolean error;
}
