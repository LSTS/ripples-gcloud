package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

@SuppressWarnings("serial")
public class UpdateFirebaseServlet extends HttpServlet {


	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		
		if (req.getPathInfo() != null
				&& req.getPathInfo().startsWith("/update")) {
			try {
				updateFirebase(resp);
				resp.getWriter().println("Assets updated from firebase.");
				resp.getWriter().close();
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE,
						"Error accessing firebase API", e);
			}
		}
		else {
			resp.sendError(400);
		}
	}

	private void updateFirebase(HttpServletResponse resp) throws Exception {
		//TODO update from https://neptus.firebaseio-demo.com/.json		
		JsonParser parser = new JsonParser();
		URL url = new URL("https://neptus.firebaseio-demo.com/.json");
		
		JsonElement root = parser.parse(new JsonReader(new InputStreamReader(url.openConnection().getInputStream())));
		Set<Entry<String, JsonElement>> assets = root.getAsJsonObject().get("assets").getAsJsonObject().entrySet();
		
		for (Entry<String, JsonElement> asset : assets) {
			try {
				long updated_at = asset.getValue().getAsJsonObject().get("updated_at").getAsLong();
				JsonElement position = asset.getValue().getAsJsonObject().get("position");
				if (position == null)
					continue;
				
				double latDegs = position.getAsJsonObject().get("latitude").getAsDouble();
				double lonDegs = position.getAsJsonObject().get("longitude").getAsDouble();
				
				long id = getId(asset.getKey());
				
				if (id == -1) {
					Logger.getGlobal().log(Level.WARNING, "Unknown system: "+asset.getKey());
					continue;
				}
				
				HubSystem sys = Store.ofy().load().type(HubSystem.class).id(id).now();
				if (sys == null) {
					sys = new HubSystem();
					sys.setImcid(id);
					sys.setName(asset.getKey());
					sys.setCreated_at(new Date(updated_at));				
				}
				sys.setUpdated_at(new Date(updated_at));
				sys.setCoordinates(new double[] { latDegs, lonDegs });
				Store.ofy().save().entity(sys);
				
				SystemPosition pos = new SystemPosition();
				pos.imc_id = id;
				pos.lat = latDegs;
				pos.lon = lonDegs;
				pos.timestamp = new Date(updated_at);
				PositionsServlet.addPosition(pos);
			}
			catch (Exception e) {
				System.err.println(asset.getKey());
				e.printStackTrace();
				continue;
			}
		}	
	}
	
	private long getId(String assetName) {
		Address addr = Store.ofy().load().type(Address.class).filter("name", assetName).first().now();
		if (addr == null)
			return -1;		
		return addr.imc_id;
	}
}
