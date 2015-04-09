package pt.lsts.ripples.model.map;

import java.net.URL;
import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class PointOfInterest {

	@Id
	public Long id;
	
	public Date creation_date = new Date();
	public Date expiration_date = new Date(System.currentTimeMillis() + 3600 * 1000 * 3);
	
	public String author = "";
	public String description = "";
	public double[] coordinates = new double[] {0,0};
	public URL resource = null;	
}
