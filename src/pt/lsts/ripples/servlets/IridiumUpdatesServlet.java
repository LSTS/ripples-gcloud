package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.IridiumSubscription;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.iridium.DeviceUpdate;
import pt.lsts.ripples.model.iridium.IridiumMessage;
import pt.lsts.ripples.model.iridium.Position;
import pt.lsts.ripples.util.IridiumUtils;

@SuppressWarnings("serial")
public class IridiumUpdatesServlet extends HttpServlet {

	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		List<IridiumSubscription> subscribers = Store.ofy().load().type(IridiumSubscription.class).list();
		
		for (IridiumSubscription s : subscribers) {
			long ellapsed = System.currentTimeMillis() - s.lastUpdateTime;
			
			if (ellapsed > s.update_period) {
				Logger.getLogger(getClass().getName()).log(Level.INFO, "Sending updates to "+s.subscriberId);
				try {
					sendUpdates(s.imei, s.lastUpdateTime);
					s.lastUpdateTime = System.currentTimeMillis();
					Store.ofy().save().entity(s).now();
				}
				catch (Exception e) {
					Logger.getLogger(getClass().getName()).log(Level.WARNING, "Could not send device updates", e);
				}
			}
		}
	}
	
	public static int sendToSubscribers(IridiumMessage msg) throws Exception {
		String imei = IridiumUtils.getIMEI(msg.getSource());
		
		List<IridiumSubscription> subscribers = Store.ofy().load().type(IridiumSubscription.class).list();
		int count = 0;
		for (IridiumSubscription s : subscribers) {
			if (!s.imei.equals(imei)) {
				Logger.getLogger(IridiumUpdatesServlet.class.getName()).log(Level.INFO, "Forwarding "+msg.getClass().getSimpleName()+" to "+s.imei);
				IridiumUtils.sendviaRockBlock(s.imei, msg.serialize());
				count++;
			}
			else {
				Logger.getLogger(IridiumUpdatesServlet.class.getName()).log(Level.INFO, "Not forwarding Iridium message to "+s.imei+" because source is the same");
				
				Logger.getGlobal().log(Level.WARNING, "Not forwarding Iridium message to "+s.imei+" because source is the same");	
			}
			
		}		
		return count;
	}
	
	private void sendUpdates(String imei, long time_since) throws Exception {
		Date d = new Date(time_since);
		List<HubSystem> systems = Store.ofy().load().type(HubSystem.class)
				.filter("updated_at >=", d).order("-updated_at").list();
		DeviceUpdate devUpdate = new DeviceUpdate();
		for (HubSystem s : systems) {
			Position pos = new Position();
			pos.id = (int)s.getImcid();
			pos.latRads = Math.toRadians(s.getCoordinates()[0]);
			pos.lonRads = Math.toRadians(s.getCoordinates()[1]);
			pos.timestamp = s.getUpdated_at().getTime()/1000.0;
			pos.posType = Position.fromImcId((int)s.getImcid());
			devUpdate.getPositions().put(pos.id, pos);
		}
		
		IridiumUtils.sendviaRockBlock(imei, devUpdate.serialize());
	}
}
