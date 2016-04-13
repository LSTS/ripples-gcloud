package pt.lsts.ripples.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class HistoricDatum {

	@Id
	private Long uid;
	
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

