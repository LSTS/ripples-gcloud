package pt.lsts.ripples.servlets;

import java.util.Date;
import java.util.logging.Logger;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.iridium.ActivateSubscription;
import pt.lsts.ripples.model.iridium.DeactivateSubscription;
import pt.lsts.ripples.model.iridium.DeviceUpdate;
import pt.lsts.ripples.model.iridium.ExtendedDeviceUpdate;
import pt.lsts.ripples.model.iridium.Position;

public class IridiumMsgHandler {

	public static void on(DeviceUpdate devUpdate) {
		Logger.getGlobal().info("DeviceUpdate");
		for (Position p : devUpdate.getPositions().values())
			setPosition(p);		
	}

	private static void setPosition(Position p) {
		long id = p.id;
		HubSystem sys = Store.ofy().load().type(HubSystem.class).id(id)
				.now();
		if (sys == null) {
			sys = new HubSystem();
			sys.setImcid(id);
			Address addr = Store.ofy().load().type(Address.class).id(id)
					.now();
			if (addr != null)
				sys.setName(addr.name);
			else
				sys.setName(IMCDefinition.getInstance().getResolver()
						.resolve((int) id));
			sys.setCreated_at(new Date());
		}
		sys.setUpdated_at(new Date());
		sys.setCoordinates(new double[] { Math.toDegrees(p.latRads),
				Math.toDegrees(p.lonRads) });
		
		Store.ofy().save().entity(sys);
	}

	public static void on(ExtendedDeviceUpdate devUpdate) {
		Logger.getGlobal().info("ExtendedDeviceUpdate");
		for (Position p : devUpdate.getPositions().values())
			setPosition(p);	
	}

	public static void on(ActivateSubscription sub) {
		Logger.getGlobal().info("ActivateSub");
	}

	public static void on(DeactivateSubscription unsb) {
		Logger.getGlobal().info("DeactivateSub");
	}
}
