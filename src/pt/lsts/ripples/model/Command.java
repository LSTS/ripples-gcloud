package pt.lsts.ripples.model;

import java.util.Date;

import com.google.appengine.api.datastore.Blob;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class Command {

	@Id
	private Long uid;
	
	@Index
	public long imc_id_source;
	
	@Index
	public long imc_id_dest;
	
	@Index
	public double timeout;
	
	@Index
	public Date timestamp;
	
	public Blob cmd = null;
}
