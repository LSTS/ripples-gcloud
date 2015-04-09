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

import pt.lsts.ripples.model.JsonUtils;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.map.PointOfInterest;

public class PoiServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain");

		Date d = new Date();
		List<PointOfInterest> pois = Store.ofy().load().type(PointOfInterest.class)
				.filter("expiration_date >=", d).list();
		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write(JsonUtils.getGsonInstance().toJson(pois));
		resp.getWriter().close();
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		try {
			PointOfInterest poi = JsonUtils.getGsonInstance().fromJson(
				new InputStreamReader(req.getInputStream()),
				PointOfInterest.class);
			
			poi.creation_date = new Date();
			Store.ofy().save().entity(poi).now();
			Logger.getLogger(getClass().getName()).log(Level.FINE, "POI was added.");
			resp.setStatus(200);
			resp.setContentType("application/json");
			resp.getWriter().write(JsonUtils.getGsonInstance().toJson(poi));
			resp.getWriter().close();
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error handling " + req.getPathInfo(), e);
			resp.setStatus(400);
			resp.getWriter().close();
		}
		
	}
}
