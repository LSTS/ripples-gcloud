package pt.lsts.ripples.model;

import com.googlecode.objectify.annotation.Subclass;

@Subclass(index=true)
public class TelemetrySample extends HistoricDatum {	
	
	public double roll, pitch, yaw, altitude, speed;	
	
}
