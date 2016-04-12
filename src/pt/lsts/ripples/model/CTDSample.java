package pt.lsts.ripples.model;

import com.googlecode.objectify.annotation.Subclass;

@Subclass(index=true)
public class CTDSample extends HistoricDatum {
	
	public double temperature, conductivity, depth;
	
}
