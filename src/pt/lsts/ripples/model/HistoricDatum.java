package pt.lsts.ripples.model;

import java.io.StringWriter;
import java.util.Date;

import javax.xml.bind.JAXB;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
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
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		JAXB.marshal(this, writer);
		return writer.toString();
	}
	
	public String toJSON() {
		return JsonUtils.getGsonInstance().toJson(this);
	}
	 
}

