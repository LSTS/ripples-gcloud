package pt.lsts.ripples.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Cache
public class Address {
	@Id
	public Long imc_id;	
	public String name;
	public String imei = null;
}