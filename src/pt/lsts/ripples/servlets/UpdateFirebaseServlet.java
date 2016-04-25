package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;
import pt.lsts.ripples.util.SystemUtils;

public class UpdateFirebaseServlet extends HttpServlet {

	private static final long serialVersionUID = -4806879026752593995L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		try {
			updateFirebase(resp);
			resp.getWriter().println("Assets updated from firebase.");
			resp.getWriter().close();
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error accessing firebase API", e);
		}

	}

	private void updateFirebase(HttpServletResponse resp) throws Exception {
		JsonParser parser = new JsonParser();
		URL url = new URL("https://neptus.firebaseio.com/.json");
		
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
				
				long id = SystemUtils.getOrGuessId(asset.getKey());
				
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
				else if (sys.getUpdated_at().after(new Date(updated_at)))
					return;
				sys.setName(asset.getKey());
				sys.setUpdated_at(new Date(updated_at));
				sys.setCoordinates(new double[] { latDegs, lonDegs });
				Store.ofy().save().entity(sys).now();
				
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
}
