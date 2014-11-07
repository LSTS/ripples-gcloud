package pt.lsts.ripples.servlets;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.ripples.firebase.FirebaseDB;
import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.iridium.ActivateSubscription;
import pt.lsts.ripples.model.iridium.DeactivateSubscription;
import pt.lsts.ripples.model.iridium.DeviceUpdate;
import pt.lsts.ripples.model.iridium.ExtendedDeviceUpdate;
import pt.lsts.ripples.model.iridium.IridiumMessage;
import pt.lsts.ripples.model.iridium.Position;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class IridiumMsgHandler {

	private static EventBus iridiumBus = new EventBus();
	static {
		iridiumBus.register(new IridiumMsgHandler());
	}

	public static void setMessage(IridiumMessage msg) {
		iridiumBus.post(msg);
	}
	
	@Subscribe
	public void on(IridiumMessage msg) {
		for (IMCMessage m : msg.asImc())
			FirebaseDB.setMessage(m);
	}

	@Subscribe
	public void on(DeviceUpdate devUpdate) {
		Logger.getGlobal().info("Handling DeviceUpdate");
		try {
			for (Position p : devUpdate.getPositions().values())
				setPosition(p);
		} catch (Exception e) {
			Logger.getGlobal().log(Level.WARNING,
					"Error handling DeviceUpdate", e);
		}
	}

	@Subscribe
	public void on(ExtendedDeviceUpdate devUpdate) {
		Logger.getGlobal().info("Handling ExtendedDeviceUpdate");
		try {
			for (Position p : devUpdate.getPositions().values())
				setPosition(p);
		} catch (Exception e) {
			Logger.getGlobal().log(Level.WARNING,
					"Error handling DeviceUpdate", e);
		}
	}
	
	private void setPosition(Position p) {
		long id = p.id;
		HubSystem sys = Store.ofy().load().type(HubSystem.class).id(id).now();
		if (sys == null) {
			sys = new HubSystem();
			sys.setImcid(id);
			Address addr = Store.ofy().load().type(Address.class).id(id).now();
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

	@Subscribe
	public void on(ActivateSubscription sub) {
		Logger.getGlobal().info("Handling ActivateSub");
	}

	@Subscribe
	public void on(DeactivateSubscription unsb) {
		Logger.getGlobal().info("Handling DeactivateSub");
	}

	public static void main(String[] args) throws Exception {
		IridiumMessage msg = IridiumMessage
				.deserialize(new HexBinaryAdapter()
						.unmarshal("1e00ffffd10701415d9f5354bc11f201dfecfdfe0e415b9f53547e707402532c7bff0101599f535412747402b82a7bff1c005c9f53548070740291277bff1500559f53549f717402ee267bff1e00579f5354276e74024b297bff12805e9f5354367074021d2c7bff1a805b9f53541c707402ef2b7bff"));
		IridiumMsgHandler.setMessage(msg);
	}
}
