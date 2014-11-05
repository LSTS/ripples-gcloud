package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import pt.lsts.ripples.model.HubIridiumMsg;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.iridium.DeviceUpdate;
import pt.lsts.ripples.model.iridium.ExtendedDeviceUpdate;
import pt.lsts.ripples.model.iridium.IridiumMessage;

public class Rock7Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final HexBinaryAdapter hexAdapter = new HexBinaryAdapter();
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yy-MM-dd HH:mm:ss");
	static {
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String data = req.getParameter("data");
		String imei = req.getParameter("imei");
		String transmit_time = req.getParameter("transmit_time");
		System.out.println("====");
		try {
			byte[] dataArr = hexAdapter.unmarshal(data);
			IridiumMessage msg = IridiumMessage.deserialize(dataArr);
			
			if (msg != null) {
				HubIridiumMsg m = new HubIridiumMsg();
				m.setMsg(data);
				m.setType(msg.getMessageType());
				m.setCreated_at(dateFormat.parse(transmit_time));
				m.setUpdated_at(new Date());
				Store.ofy().save().entity(m);
				
				switch (msg.getClass().getSimpleName()) { 
				case "DeviceUpdate":
					IridiumMsgHandler.on((DeviceUpdate)msg);
					break;
				case "ExtendedDeviceUpdate":
					IridiumMsgHandler.on((ExtendedDeviceUpdate)msg);
					break;
				default:
					break;
				}

				HubSystem system = Store.ofy().load().type(HubSystem.class).id(msg.getSource()).now();

				if (system != null && !imei.equals(system.getIridium())) {
					system.setIridium(imei);
					Store.ofy().save().entity(system);
				}
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		resp.getWriter().write("200 OK");
		resp.setStatus(200);
		resp.getWriter().close();
	}
}
