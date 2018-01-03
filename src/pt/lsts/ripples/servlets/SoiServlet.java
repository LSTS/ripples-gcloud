package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.soi.SoiState;

public class SoiServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain");

		Date d = new Date(System.currentTimeMillis() - 3600 * 4 * 1000);
		
		ArrayList<String> toShow = new ArrayList<String>();
		
		List<SoiState> states = Store.ofy().load().type(SoiState.class)
				.list();
		
		for (SoiState state : states) {
			if (state.lastUpdated.after(d))
				toShow.add(state.assetState);
		}
		
		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write("[");
		
		if (!toShow.isEmpty())
			resp.getWriter().write(", "+toShow.get(0));
		
		for (int i = 1; i < toShow.size(); i++)
			resp.getWriter().write(", "+toShow.get(i));
		
		resp.getWriter().write("]\n");
		resp.getWriter().close();
	}
}
