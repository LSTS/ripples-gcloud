package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mortbay.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.lsts.ripples.model.Address;
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
				Log.warn("Error fetching addresses", e);
				resp.setStatus(500);
			}
		}
		
		resp.setContentType("application/json");
		resp.getWriter().write(
				JsonUtils.getGsonInstance().toJson(
						Store.ofy().load().type(Address.class).list()));
		resp.getWriter().close();
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

			Address address = new Address();
			address.imc_id = id;
			address.name = name;
			if (existing != null)
				address.imei = existing.imei;

			Store.ofy().save().entity(address);
			count++;
			if (existing == null)
				log("Created a new address entry for " + address.name);
		}
		log("Stored " + count + " addresses in the datastore.");
	}
}
