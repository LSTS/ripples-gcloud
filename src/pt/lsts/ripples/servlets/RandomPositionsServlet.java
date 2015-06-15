package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.ripples.model.SystemPosition;

public class RandomPositionsServlet extends HttpServlet {

	private static final long serialVersionUID = -6005727933482433699L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Random r = new Random(System.currentTimeMillis());
		
		double lat = r.nextGaussian() * 0.1 + 41;
		double lon = r.nextGaussian() * 0.1 -8;
		double movement_x = r.nextGaussian() * 0.00001 - 0.000005;
		double movement_y = r.nextGaussian() * 0.00001 - 0.000005;
		resp.setContentType("text/plain");
		for (int i = -10; i < 1; i++) {
			SystemPosition pos = new SystemPosition();
			pos.imc_id = 22;
			pos.lat = lat + i * movement_x;
			pos.lon = lon + i * movement_y;
			pos.timestamp = new Date(System.currentTimeMillis() + 1000 * i);
			PositionsServlet.addPosition(pos);			
		}		
		
		resp.getWriter().write("Added 10 positions to system 22");
		resp.getWriter().close();		
	}
}
