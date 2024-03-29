package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import pt.lsts.endurance.Asset;
import pt.lsts.endurance.AssetState;
import pt.lsts.endurance.Plan;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.soi.SoiState;
import pt.lsts.ripples.model.soi.VerticalProfileData;

public class SoiServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private void sendProfiles(int hours, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		Date d = new Date(System.currentTimeMillis() - 3600 * hours * 1000);

		List<VerticalProfileData> profiles = Store.ofy().load().type(VerticalProfileData.class)
				.filter("timestamp >=", d).order("timestamp").list();
		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write("[");

		if (!profiles.isEmpty())
			resp.getWriter().write(profiles.get(0).toString());

		for (int i = 1; i < profiles.size(); i++) {
			resp.getWriter().write("," + profiles.get(i).toString());
		}
		resp.getWriter().write("]\n");
		resp.getWriter().close();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");

		Date d = new Date(System.currentTimeMillis() - 3600 * 4 * 1000);

		if ("/profiles".equals(req.getPathInfo())) {
			sendProfiles(4, req, resp);
			return;
		}
		
		boolean debug = "/debug".equals(req.getPathInfo());
		
		ArrayList<String> toShow = new ArrayList<String>();

		List<SoiState> states = Store.ofy().load().type(SoiState.class).list();

		for (SoiState state : states) {
			if (state.lastUpdated.after(d))
				toShow.add(state.asset);
		}
		
		if (debug) {
			resp.setContentType("text/plain");
			resp.setStatus(200);
			
			for (SoiState state : states) {
				resp.getWriter().write(state.asset.toString()+" :: "+state.lastUpdated+" vs "+d+" \n\n");
			}
			resp.getWriter().close();
		}
		else {
			resp.setContentType("application/json");
			resp.setStatus(200);
			resp.getWriter().write("[");
		}
		
		if (!toShow.isEmpty())
			resp.getWriter().write(toShow.get(0));

		for (int i = 1; i < toShow.size(); i++)
			resp.getWriter().write(", " + toShow.get(i));

		resp.getWriter().write("]\n");
		resp.getWriter().close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

		Logger.getLogger(getClass().getSimpleName()).info("Received post request!");

		try {
			InputStreamReader reader = new InputStreamReader(req.getInputStream());
			JsonObject val = Json.parse(reader).asObject();
			String vehicle = val.getString("name", "");
			SoiState state = Store.ofy().load().type(SoiState.class).id(vehicle).now();

			if (state == null) {
				state = new SoiState();
				state.name = vehicle;
			}
			Asset asset;
			if (state.asset != null)
				asset = Asset.parse(state.asset);
			else
				asset = new Asset(vehicle);

			if (val.get("plan") != null)
				asset.setPlan(Plan.parse(val.get("plan").toString()));

			if (val.get("lastState") != null)
				asset.setState(AssetState.parse(val.get("lastState").toString()));

			state.asset = asset.toString();
			state.lastUpdated = new Date();

			Store.ofy().save().entity(state).now();
			Logger.getLogger(getClass().getSimpleName()).info("Updated state: " + state);

			resp.setStatus(200);
			reader.close();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
