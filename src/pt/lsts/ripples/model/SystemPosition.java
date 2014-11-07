package pt.lsts.ripples.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class SystemPosition {

	@Id
	Long id;
	
	@Index
	public long imc_id;
	
	@Index
	public Date timestamp;
	
	public double lat;
	public double lon;	
}
