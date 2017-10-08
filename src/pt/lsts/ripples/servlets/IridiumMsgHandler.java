package pt.lsts.ripples.servlets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import pt.lsts.imc.HistoricData;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.LogBookEntry;
import pt.lsts.imc.SoiState;
import pt.lsts.ripples.model.IridiumSubscription;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;
import pt.lsts.ripples.model.iridium.ActivateSubscription;
import pt.lsts.ripples.model.iridium.DeactivateSubscription;
import pt.lsts.ripples.model.iridium.DeviceUpdate;
import pt.lsts.ripples.model.iridium.ExtendedDeviceUpdate;
import pt.lsts.ripples.model.iridium.ImcIridiumMessage;
import pt.lsts.ripples.model.iridium.IridiumMessage;
import pt.lsts.ripples.model.iridium.Position;
import pt.lsts.ripples.model.log.LogEntry;
import pt.lsts.ripples.model.log.MissionLog;
import pt.lsts.ripples.servlets.datastore.HistoricDataProcessor;
import pt.lsts.ripples.util.IridiumUtils;

public class IridiumMsgHandler {

	public static void setMessage(String imei, IridiumMessage msg) {
		Integer id = msg.getSource();
		
		try {
			id = IridiumUtils.getImcId(imei);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (id != null)
			msg.setSource(id);
		
		on(msg);
		switch (msg.message_type) {
		case IridiumMessage.TYPE_DEVICE_UPDATE:
			on((DeviceUpdate)msg);			
			break;
		case IridiumMessage.TYPE_EXTENDED_DEVICE_UPDATE:
			on((ExtendedDeviceUpdate)msg);
			break;
		case IridiumMessage.TYPE_ACTIVATE_SUBSCRIPTION:
			on(imei, (ActivateSubscription)msg);
			break;
		case IridiumMessage.TYPE_DEACTIVATE_SUBSCRIPTION:
			on((DeactivateSubscription)msg);
			break;
		case IridiumMessage.TYPE_IMC_IRIDIUM_MESSAGE:
			on((ImcIridiumMessage)msg);			
			break;
		default:
			break;
		}
	}
	
	public static  void on(IridiumMessage msg) {
//		for (IMCMessage m : msg.asImc()) {
//			try {
//				FirebaseDB.setMessage(m);
//			}
//			catch (Exception e) {
//				Logger.getLogger(IridiumMsgHandler.class.getName()).log(Level.SEVERE, "Error posting to Firebase", e);
//			}
//		}			
	}
	
	public static  void on(ImcIridiumMessage msg) {
		IMCMessage m = msg.getMsg();
		m.setSrc(msg.getSource());
		switch (m.getMgid()) {
		case LogBookEntry.ID_STATIC:
			addLogEntry((LogBookEntry)m);
			break;
		case HistoricData.ID_STATIC:
			try {
				HistoricDataProcessor.processData(new HistoricData(m));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case SoiState.ID_STATIC:
			incoming((SoiState)m);
			break;
		default:
			break;
		}
	}
	
	private static void incoming(SoiState state) {
		System.out.println("Received the following state from "+state.getSourceName()+":");
		System.out.println(state.asJSON());				
	}
	
	private static void addLogEntry(LogBookEntry entry) {
		String date = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
		MissionLog l = Store.ofy().load().type(MissionLog.class).id(date).now();
		if (l == null)
			l = new MissionLog();
		
		LogEntry e = new LogEntry();
		e.author = entry.getSourceName();
		e.text = entry.getText();
		e.timestamp = entry.getTimestampMillis();
		l.log.add(e);
		Store.ofy().save().entity(l).now();
	}

	public static void on(DeviceUpdate devUpdate) {
		Logger.getLogger(IridiumMsgHandler.class.getName()).info("Handling DeviceUpdate");
		try {
			System.out.println(devUpdate.getPositions().size());
			for (Position p : devUpdate.getPositions().values()) 
				setPosition(p);
		} catch (Exception e) {
			Logger.getLogger(IridiumMsgHandler.class.getName()).log(Level.WARNING,
					"Error handling DeviceUpdate", e);
		}
	}

	public static void on(ExtendedDeviceUpdate devUpdate) {
		Logger.getLogger(IridiumMsgHandler.class.getName()).info("Handling ExtendedDeviceUpdate");
		try {
			for (Position p : devUpdate.getPositions().values())
				setPosition(p);
		} catch (Exception e) {
			Logger.getLogger(IridiumMsgHandler.class.getName()).log(Level.WARNING,
					"Error handling DeviceUpdate", e);
		}
	}
	
	private static void setPosition(Position p) {
		SystemPosition pos = new SystemPosition();
		pos.imc_id = p.id;
		pos.lat = Math.toDegrees(p.latRads);
		pos.lon = Math.toDegrees(p.lonRads);
		pos.timestamp = new Date((long)(p.timestamp * 1000));
		PositionsServlet.addPosition(pos, false);
	}

	public static void on(String imei, ActivateSubscription sub) {
		Logger.getLogger(IridiumMsgHandler.class.getName()).info("Handling ActivateSub");
		
		List<IridiumSubscription> subscribers = Store.ofy().load().type(IridiumSubscription.class).list();
		IridiumSubscription subscription = null;
		
		for (IridiumSubscription s : subscribers) {
			if (s.subscriberId == sub.source) {
				subscription = s;
				if (imei != null) {
					s.imei = imei;
					Store.ofy().save().entity(s).now();
				}
				break;
			}
		}
		if (subscription == null) {
			subscription = new IridiumSubscription();
			subscription.subscriberId = sub.source;
			subscription.imei = imei;
		}
		subscription.lastUpdateTime = 0;
		Store.ofy().save().entity(subscription).now();
		Logger.getGlobal().log(Level.INFO, sub.source+" has subscribed.");		
	}

	public static void on(DeactivateSubscription unsb) {
		Logger.getLogger(IridiumMsgHandler.class.getName()).info("Handling DeactivateSub");
		
		List<IridiumSubscription> subscribers = Store.ofy().load().type(IridiumSubscription.class).list();
		IridiumSubscription toDelete = null;
		for (IridiumSubscription s : subscribers) {
			if (s.subscriberId == unsb.source) {
				toDelete = s;
				break;
			}
		}
		if (toDelete != null) {
			Store.ofy().delete().entity(toDelete).now();
			Logger.getGlobal().log(Level.INFO, "Removed subscription from "+toDelete.subscriberId);
		}
		else {
			Logger.getGlobal().log(Level.WARNING, "Can not remove subscription from "+unsb.source+": not found.");
		}

	}

	public static void main(String[] args) throws Exception {
		IridiumMessage msg = IridiumMessage
				.deserialize(new HexBinaryAdapter()
						.unmarshal("1e00ffffd10701415d9f5354bc11f201dfecfdfe0e415b9f53547e707402532c7bff0101599f535412747402b82a7bff1c005c9f53548070740291277bff1500559f53549f717402ee267bff1e00579f5354276e74024b297bff12805e9f5354367074021d2c7bff1a805b9f53541c707402ef2b7bff"));
		IridiumMsgHandler.setMessage(null, msg);
	}
}
