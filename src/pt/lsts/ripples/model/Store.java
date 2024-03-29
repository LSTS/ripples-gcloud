package pt.lsts.ripples.model;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import pt.lsts.ripples.model.ctd.EnvironmentalData;
import pt.lsts.ripples.model.log.MissionLog;
import pt.lsts.ripples.model.map.PointOfInterest;
import pt.lsts.ripples.model.soi.SoiState;
import pt.lsts.ripples.model.soi.VerticalProfileData;

public class Store {

	static {
		ObjectifyService.factory().register(HubSystem.class);
		ObjectifyService.factory().register(HubIridiumMsg.class);
		ObjectifyService.factory().register(Address.class);
		ObjectifyService.factory().register(ArgosPosition.class);        
		ObjectifyService.factory().register(Credentials.class);
		ObjectifyService.factory().register(SystemPosition.class);
		ObjectifyService.factory().register(MissionLog.class);
		ObjectifyService.factory().register(PointOfInterest.class);
		ObjectifyService.factory().register(BuoyAddress.class);
		ObjectifyService.factory().register(IridiumSubscription.class);
		ObjectifyService.factory().register(HistoricDatum.class);
		ObjectifyService.factory().register(CTDSample.class);
		ObjectifyService.factory().register(TelemetrySample.class);
		ObjectifyService.factory().register(EventSample.class);
		ObjectifyService.factory().register(Command.class);
		ObjectifyService.factory().register(DataRoute.class);
		ObjectifyService.factory().register(SoiState.class);
		ObjectifyService.factory().register(VerticalProfileData.class);
		ObjectifyService.factory().register(EnvironmentalData.class);
	}
	
	public static Objectify ofy() {
		return ObjectifyService.ofy();
	}
	
	public static ObjectifyFactory factory() {
	    return (ObjectifyFactory) ObjectifyService.factory();
	}
}
