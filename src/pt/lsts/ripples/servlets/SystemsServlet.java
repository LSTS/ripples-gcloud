package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.JsonUtils;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;

/**
 * Systems service, according to HUB API V1.
 * 
 * @author zp
 */
public class SystemsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * Parse the URL path and respond accordingly
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");

		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
			listSystems(req, resp);
		} else {
			getSystem(req.getPathInfo().substring(1), req, resp);
		}
		resp.getWriter().close();
	}

	/**
	 * Store a system state.
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			HubSystem s = JsonUtils.getGsonInstance().fromJson(
					new InputStreamReader(req.getInputStream()),
					HubSystem.class);
			s.setUpdated_at(new Date());
			Store.ofy().save().entity(s).now();
			
			SystemPosition pos = new SystemPosition();
			pos.imc_id = s.getImcid();
			pos.lat = s.getCoordinates()[0];
			pos.lon = s.getCoordinates()[1];
			pos.timestamp = s.getUpdated_at();
			PositionsServlet.addPosition(pos, false);
			
			Logger.getLogger(getClass().getName()).log(Level.FINE, "System " + s.getName() + " was updated.");
			resp.setStatus(200);
			resp.setContentType("application/json");
			resp.getWriter().write(JsonUtils.getGsonInstance().toJson(s));
			resp.getWriter().close();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error handling " + req.getPathInfo(), e);
			resp.setStatus(400);
			resp.getWriter().close();
		}

	}

	/**
	 * Retrieve a specific system (provided on the path)
	 */
	public void getSystem(String system, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {

		if (system.equals("active"))
			listActiveSystems(req, resp);
		else {
			try {
				long id = Long.parseLong(system);
				HubSystem s = Store.ofy().load().type(HubSystem.class).id(id).now();
				if (s == null)
					resp.setStatus(404);
				else {
					resp.setContentType("application/json");
					resp.setStatus(200);
					resp.getWriter().write(JsonUtils.getGsonInstance().toJson(s));
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error handling " + req.getPathInfo(), e);
				resp.setStatus(400);
			}
		}
		resp.getWriter().close();
	}

	/**
	 * Retrieve only systems that were updated on the last 3 days
	 */
	public void listActiveSystems(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		Date d = new Date(System.currentTimeMillis() - 1000 * 3600 * 24);
		List<HubSystem> systems = Store.ofy().load().type(HubSystem.class)
				.filter("updated_at >=", d).order("-updated_at").list();
		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write(JsonUtils.getGsonInstance().toJson(systems));
		resp.getWriter().close();
	}

	/**
	 * Retrieve all stored systems (new and old)
	 */
	public void listSystems(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		List<HubSystem> systems = Store.ofy().load().type(HubSystem.class)
				.order("-updated_at").list();
		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write(JsonUtils.getGsonInstance().toJson(systems));
		resp.getWriter().close();
	}
}
