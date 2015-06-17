package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.JsonUtils;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;

/**
 * Systems service, according to HUB API V1.
 * 
 * @author zp
 */
public class PositionsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * Parse the URL path and respond accordingly
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");

		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
			listPositions(req, resp);
		}
		resp.getWriter().close();
	}

	/**
	 * Retrieve all stored systems (new and old)
	 */
	public void listPositions(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		ArrayList<SystemPosition> ret = new ArrayList<>();
		
		// get systems updated today
		Date d = new Date(System.currentTimeMillis() - 1000 * 3600 * 24);
		List<HubSystem> systems = Store.ofy().load().type(HubSystem.class)
				.filter("updated_at >=", d).order("-updated_at").list();
		
		// get last 25 positions for each one of them
		for (HubSystem s : systems) {
			List<SystemPosition> positions = Store
					.ofy()
					.load()
					.type(SystemPosition.class)
					.filter("timestamp >=",
							new Date(System.currentTimeMillis() - 1000 * 3600 * 24))
							.filter("imc_id =", s.imcid)
							.order("-timestamp").limit(25).list();
			for (SystemPosition pos : positions) {
				ret.add(pos);
			}
		}

		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write(JsonUtils.getGsonInstance().toJson(ret));
		resp.getWriter().close();
	}

	public static void addPosition(SystemPosition pos) {
		
		HubSystem sys = Store.ofy().load().type(HubSystem.class).id(pos.imc_id).now();
		if (sys == null) {
			sys = new HubSystem();
			sys.setImcid(pos.imc_id);
			Address addr = Store.ofy().load().type(Address.class).id(pos.imc_id).now();
			if (addr != null)
				sys.setName(addr.name);
			else
				sys.setName(IMCDefinition.getInstance().getResolver()
						.resolve((int) pos.imc_id));
			sys.setCreated_at(new Date());
		}
		sys.setUpdated_at(pos.timestamp);
		sys.setCoordinates(new double[] { pos.lat,
				pos.lon });

		Store.ofy().save().entity(sys);
		
		SystemPosition existing = Store.ofy().load().type(SystemPosition.class)
				.filter("imc_id", pos.imc_id)
				.order("-timestamp").limit(1).first().now();
		if (existing == null) { 
			Logger.getLogger(PositionsServlet.class.getName()).log(Level.INFO,
					"First position for " + pos.imc_id);
		}
		else if (existing.timestamp.getTime() > pos.timestamp.getTime()) {
			Logger.getLogger(PositionsServlet.class.getName()).log(
					Level.WARNING,
					"Already have a more updated position for " + pos.imc_id);
			return;
		}
		// log at most 1 position every 1 seconds
		else if (pos.timestamp.getTime() - existing.timestamp.getTime() < 1000) {
			Logger.getLogger(PositionsServlet.class.getName()).log(Level.FINE,
					"Already have an up to date position for " + pos.imc_id);
			return;
		}
		Logger.getLogger(PositionsServlet.class.getName()).log(
				Level.FINE,
				"Storing " + pos.imc_id);
		
		Store.ofy().save().entity(pos);		
	}
}
