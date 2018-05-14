package pt.lsts.ripples.model.ctd;

import java.util.Hashtable;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
public class EnvironmentalData {

	@Id
	public Long uid;
	
	@Index
	public long timestamp = System.currentTimeMillis();
	
	@Index
	public Long imc_id;
	
	public Double latitude, longitude;

	public Hashtable<String, Double> data = new Hashtable<>();
}
