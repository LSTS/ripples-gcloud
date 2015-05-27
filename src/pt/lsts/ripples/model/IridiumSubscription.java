package pt.lsts.ripples.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class IridiumSubscription {
	@Id
	public long subscriberId;
	public long update_period = 1000 * 60 * 10;
	public String imei;	
	public long lastUpdateTime;	
}
