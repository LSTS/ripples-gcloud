package pt.lsts.ripples.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class BuoyAddress {

	@Id
	public String id;
	
	public String ip;
	
	public String battery;
	
}
