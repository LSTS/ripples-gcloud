package pt.lsts.ripples.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class IridiumSubscription {
	@Id
	public long id;
	public String imei;
	public int subscriberId;
	public Date lastUpdateTime;	
}
