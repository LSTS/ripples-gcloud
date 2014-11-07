package pt.lsts.ripples.model;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class Store {

	static {
		ObjectifyService.factory().register(HubSystem.class);
		ObjectifyService.factory().register(HubIridiumMsg.class);
		ObjectifyService.factory().register(Address.class);
		ObjectifyService.factory().register(ArgosPosition.class);        
		ObjectifyService.factory().register(Credentials.class);
		ObjectifyService.factory().register(SystemPosition.class);
	}
	
	public static Objectify ofy() {
		return ObjectifyService.ofy();
	}
	
	public static ObjectifyFactory factory() {
	    return (ObjectifyFactory) ObjectifyService.factory();
	}
}
