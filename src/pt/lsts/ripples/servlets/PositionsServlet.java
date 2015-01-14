package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.util.Date;
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
		List<SystemPosition> positions = Store.ofy().load().type(SystemPosition.class)
				.filter("timestamp >=", new Date(System.currentTimeMillis() - 1000 * 3600 * 24))
				.order("-timestamp")
				.list();
		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write(JsonUtils.getGsonInstance().toJson(positions));
		resp.getWriter().close();
	}
	
	public static void addPosition(SystemPosition pos) {
		SystemPosition existing = Store.ofy().load().type(SystemPosition.class)
				.filter("imc_id", pos.imc_id)
				.filter("timestamp", pos.timestamp).first().now();
		
		if (existing == null) {
			Logger.getLogger(PositionsServlet.class.getName()).log(Level.INFO, "Position updated for "+pos.imc_id);
			Store.ofy().save().entity(pos);
		}
	}
}