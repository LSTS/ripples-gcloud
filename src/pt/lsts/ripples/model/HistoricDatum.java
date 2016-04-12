package pt.lsts.ripples.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;

import pt.lsts.imc.IMCMessage;

@Entity
public class HistoricDatum {

	@Index
	public long imc_id;
	
	@Index
	public long sample_type;
	
	@Index
	public Date timestamp;

	public double lat;
	public double lon;
	public double z;		
}

