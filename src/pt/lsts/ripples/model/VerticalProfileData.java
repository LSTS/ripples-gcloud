package pt.lsts.ripples.model;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
public class VerticalProfileData {
	
	@Id
	private Long uid;
	
	@Index
	public long imc_id;
	
	@Index
	public long sample_type;

	@Index
	public Date timestamp;

	public double latitude;
	public double longitude;
	
	ArrayList<Double> depths = new ArrayList<>();
	ArrayList<Double> values = new ArrayList<>();

}
