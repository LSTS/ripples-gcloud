package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;

@SuppressWarnings("serial")
public class UpdateSpotsServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		if (req.getPathInfo() != null
				&& req.getPathInfo().startsWith("/update")) {
			try {
				updateSpots();
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE,
						"Error fetching spot positions", e);
			}
		}
		resp.setContentType("text/plain");
		resp.getWriter().println("x");
		resp.getWriter().close();
	}

	private void updateSpots() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		URL url = new URL(
				"https://api.findmespot.com/spot-main-web/consumer/rest-api/2.0/public/feed/0qQz420UTPODTjoHylgIOPa3RqqvOhkMK/message.xml");
		URLConnection conn = url.openConnection();
		conn.setUseCaches(false);
		conn.connect();
		Document doc = db.parse(conn.getInputStream());

		
		LinkedHashMap<String, SystemPosition> positions = new LinkedHashMap<String, SystemPosition>();
		
		NodeList messages = doc.getElementsByTagName("message");
		for (int i = 0; i < messages.getLength(); i++) {
			NodeList elems = messages.item(i).getChildNodes();
			String name = null;
			double lat = 0, lon = 0;
			long timestamp = System.currentTimeMillis();
			//String battState = null, msgType = null;
			for (int j = elems.getLength()-1; j >= 0; j--) {
				Node nd = elems.item(j);
				switch (nd.getNodeName()) {
				case "unixTime":
					timestamp = Long.parseLong(nd.getTextContent()) * 1000;
					break;
				case "latitude":
					lat = Double.parseDouble(nd.getTextContent());
					break;
				case "longitude":
					lon = Double.parseDouble(nd.getTextContent());
					break;
				case "messengerName":
					name = nd.getTextContent().toLowerCase();
					break;
//				case "batteryState":
//					battState = nd.getTextContent();
//					break;
//				case "messageType":
//					msgType = nd.getTextContent();
//					break;
				default:
					break;
				}
			}			
			long imc_id = getId(name);
			SystemPosition pos = new SystemPosition();
			pos.imc_id = imc_id;
			pos.lat = lat;
			pos.lon = lon;
			pos.timestamp = new Date(timestamp);
			if (!positions.containsKey(name) || positions.get(name).timestamp.before(pos.timestamp))
				positions.put(name, pos);			
		}		
		
		for (String name : positions.keySet()) {
			SystemPosition pos = positions.get(name);
			HubSystem sys = Store.ofy().load().type(HubSystem.class).id(pos.imc_id).now();
			if (sys == null) {
				sys = new HubSystem();
				sys.setImcid(pos.imc_id);
				sys.setName(name);
				sys.setCreated_at(pos.timestamp);				
			}
			sys.setUpdated_at(pos.timestamp);
			sys.setCoordinates(new double[] { pos.lat, pos.lon });
			Store.ofy().save().entity(sys);
			Logger.getLogger(getClass().getName()).log(Level.INFO, "Stored position for "+name+": "+pos.timestamp);
			PositionsServlet.addPosition(pos);
		}
	}

	private long getId(String spotName) {
		Address addr = Store.ofy().load().type(Address.class).filter("name", spotName).first().now();
		if (addr == null)
			return -1;		
		return addr.imc_id;
	}
}
