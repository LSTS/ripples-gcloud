package pt.lsts.ripples.model.soi;

import java.util.ArrayList;
import java.util.Date;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;

@Entity
@Cache
public class VerticalProfileData {
	
	@Id
	private Long uid;
	
	@Index
	public long imc_id;
	
	@Index
	public String sample_type = "Temperature";

	@Index
	public Date timestamp = new Date();

	public double latitude = 0d;
	public double longitude = 0d;
	
	public ArrayList<Sample> samples = new ArrayList<Sample>();
	
	public static class Sample {
		double depth = -1;
		double value = 0;
		
		public Sample() {
			
		}
		
		public Sample(double depth, double value) {
			this.depth = depth;
			this.value = value;
		}
	}
	
	@Override
	public String toString() {
		JsonObject json = new JsonObject();
		HubSystem system = Store.ofy().load().type(HubSystem.class).id(imc_id).now();
		json.add("timestamp", ""+timestamp.getTime()/1000);
		if (system != null)
			json.add("source", system.name);
		else
			json.add("source", ""+imc_id);
		
		json.add("type", sample_type);
		json.add("latitude", ""+latitude);
		json.add("longitude", ""+longitude);
		
		JsonArray array = new JsonArray();
		for (Sample s : samples) {
			JsonObject elem = new JsonObject();
			elem.add("depth", s.depth);
			elem.add("value", s.value);
			array.add(elem);
		}
		json.add("samples", array);
		
		return json.toString();		
	}
}
