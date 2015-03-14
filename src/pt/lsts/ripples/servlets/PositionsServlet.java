package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		LinkedHashMap<Long, Integer> counters = new LinkedHashMap<>();

		List<SystemPosition> positions = Store
				.ofy()
				.load()
				.type(SystemPosition.class)
				.filter("timestamp >=",
						new Date(System.currentTimeMillis() - 1000 * 3600 * 24))
						.order("-timestamp").list();

		for (int i = positions.size() -1; i >= 0; i--) {
			SystemPosition p = positions.get(i);
			if (!counters.containsKey(p.imc_id))
				counters.put(p.imc_id, 0);
			
			int count = counters.get(p.imc_id);
			if (count >= 1)
				continue;
			counters.put(p.imc_id, ++count);
			ret.add(0, p);			
		}

		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write(JsonUtils.getGsonInstance().toJson(ret));
		resp.getWriter().close();
	}

	public static void addPosition(SystemPosition pos) {
		SystemPosition existing = Store.ofy().load().type(SystemPosition.class)
				.filter("imc_id", pos.imc_id)
				.filter("timestamp", pos.timestamp).first().now();
		if (existing == null)
			Logger.getLogger(PositionsServlet.class.getName()).log(Level.INFO,
					"First position for " + pos.imc_id);

		else if (existing.timestamp.getTime() > pos.timestamp.getTime())
			Logger.getLogger(PositionsServlet.class.getName()).log(
					Level.WARNING,
					"Already have a more updated position for " + pos.imc_id);

		// log at most 1 position every 1 seconds
		else if (pos.timestamp.getTime() - existing.timestamp.getTime() < 1000)
			Logger.getLogger(PositionsServlet.class.getName()).log(Level.FINE,
					"Already have an up to date position for " + pos.imc_id);

		else
			Store.ofy().save().entity(pos);
	}
}
