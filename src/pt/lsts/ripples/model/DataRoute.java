package pt.lsts.ripples.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
public class DataRoute {

	@Id
	private Long uid;
	
	@Index
	public long system;
	
	@Index
	public long gateway;
	
	public long timestamp;	
}
