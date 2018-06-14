package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
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
import pt.lsts.ripples.model.JsonUtils;
import pt.lsts.ripples.model.Store;

public class AddressesServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String location = "https://raw.githubusercontent.com/LSTS/imc/master/IMC_Addresses.xml";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		if ("/update".equals(req.getPathInfo())) {
			try {
				updateAddresses();
				resp.setStatus(200);
	
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error fetching addresses", e);
				resp.setStatus(500);
				return;
			}
		}
		else if ("/updateWavy".equals(req.getPathInfo())) {
			try {
				setWavyAddresses();
				resp.setStatus(200);
	
			} catch (Exception e) {
				resp.setStatus(500);
				e.printStackTrace(resp.getWriter());
				resp.getWriter().close();
				e.printStackTrace();
				return;
			}
		}
		else if ("/clear".equals(req.getPathInfo())) {
			try {
				resetAddresses();
				resp.setStatus(200);
	
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error fetching addresses", e);
				resp.setStatus(500);
				return;
			}
		}
		
		resp.setContentType("application/json");
		resp.getWriter().write(
				JsonUtils.getGsonInstance().toJson(
						Store.ofy().load().type(Address.class).list()));
		resp.getWriter().close();
	}

	private void setWavyAddresses() {
		
		LinkedHashMap<String, Long> addrs = new LinkedHashMap<String, Long>();
		addrs.put("+351915733675", 0x8500+01l);
		addrs.put("+351915733747", 0x8500+10l);
		addrs.put("+351915733656", 0x8500+11l);
		addrs.put("+351965655364", 0x8500+12l);
		addrs.put("+351915733646", 0x8500+13l);
		addrs.put("+351915733603", 0x8500+14l);
		addrs.put("+351915733713", 0x8500+15l);
		addrs.put("+351964660645", 0x8500+16l);
		addrs.put("+351915733646", 0x8500+17l);
		addrs.put("+351915733635", 0x8500+18l);
		addrs.put("+351915733612", 0x8500+19l);
		addrs.put("+351915733481", 0x8500+20l);
		addrs.put("+351915733750", 0x8500+21l);
		addrs.put("+351915733405", 0x8500+22l);
		addrs.put("+351915733574", 0x8500+23l);
		addrs.put("+351915733662", 0x8500+24l);
		addrs.put("+351915733418", 0x8500+25l);
		addrs.put("+351915733657", 0x8500+26l);
		addrs.put("+351915733619", 0x8500+27l);
		addrs.put("+351915733474", 0x8500+28l);
		addrs.put("+351915733621", 0x8500+29l);
		addrs.put("+351915733584", 0x8500+30l);
		addrs.put("+351915733747", 0x8500+40l);
		addrs.put("+351915733695", 0x8500+41l);
		
		int count = 0;
		for (Entry<String, Long> addr : addrs.entrySet()) {
			Address existing = Store.ofy().load().type(Address.class).id(addr.getValue())
					.now();

			if (existing == null) {
				Address address = new Address();
				address.imc_id = addr.getValue();
				address.name = "wavy-"+String.format("%02d", addr.getValue() - 0x8500);
				address.phone = addr.getKey();
				Store.ofy().save().entity(address);
				count++;
			}			
		}	
		Logger.getLogger(getClass().getName()).info("Stored " + count + " addresses in the datastore.");
	}
	
	private void resetAddresses() throws Exception {
		Logger.getLogger(getClass().getSimpleName()).warning("Deleting all stored systems!");
		
		List<HubSystem> systems = Store.ofy().load().type(HubSystem.class).list();
		Store.ofy().delete().entities(systems);
		
		List<Address> addresses = Store.ofy().load().type(Address.class).list();
		Store.ofy().delete().entities(addresses);
		
		updateAddresses();
		setWavyAddresses();
		
	}
	
	private void updateAddresses() throws Exception {

		URL url = new URL(location);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(url.openStream());

		NodeList addresses = doc.getElementsByTagName("address");
		int count = 0;
		for (int i = 0; i < addresses.getLength(); i++) {
			Node nd = addresses.item(i);

			String idHex = nd.getAttributes().getNamedItem("id")
					.getTextContent();
			String name = nd.getAttributes().getNamedItem("name")
					.getTextContent();
			long id = Long.parseLong(idHex.replaceAll("0x", ""), 16);

			if (id == 0)
				continue;

			Address existing = Store.ofy().load().type(Address.class).id(id)
					.now();
			
			HubSystem system = Store.ofy().load().type(HubSystem.class).id(id)
					.now();
			
			if (existing == null) {
			    existing = Store.ofy().load().type(Address.class).filter("name", name).first().now();
			}

			Address address = new Address();
			address.imc_id = id;
			address.name = name;
			if (existing != null)
				address.imei = existing.imei;
			
			

			Store.ofy().save().entity(address);
			if (system != null && !system.name.equals(address.name)) {
				system.name = address.name;
				Store.ofy().save().entity(system);
			}
			count++;
			if (existing == null)
				Logger.getLogger(getClass().getName()).info("Created a new address entry for " + address.name);
		}
		Logger.getLogger(getClass().getName()).info("Stored " + count + " addresses in the datastore.");
	}
}
