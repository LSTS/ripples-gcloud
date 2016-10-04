package pt.lsts.ripples.servlets.raia;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;
import pt.lsts.ripples.servlets.PositionsServlet;

public class WavyUpdateServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*
	 * 0 1 2 3 4 5 6
	 * http://wavy.inesctec.pt:80/u?v=1|14:52:32|N41.178329|W8.595937|3773|0|171
	 * .99 3773 -> battery level 0 -> battery percentage 171.9 -> altitude
	 */

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			String query = req.getQueryString();
			if (query == null) 
				throw new Exception("You must provide a query string!");

			String[] parts = URLDecoder.decode(query, "UTF-8").split("\\|");
			if (parts.length != 7)
				throw new Exception("Number of parts received ("+parts.length+") is not 7");
			
			int id = Integer.parseInt(parts[0].split("=")[1]);

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date time = sdf.parse(parts[1]);
			Date today = new Date();
			GregorianCalendar calToday = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			calToday.setTime(today);
			GregorianCalendar calWavy = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			calWavy.setTime(time);
			calWavy.set(GregorianCalendar.YEAR, calToday.get(GregorianCalendar.YEAR));
			calWavy.set(GregorianCalendar.MONTH, calToday.get(GregorianCalendar.MONTH));
			calWavy.set(GregorianCalendar.DAY_OF_MONTH, calToday.get(GregorianCalendar.DAY_OF_MONTH));
			time.setTime(calWavy.getTimeInMillis());
			
			double lat = parts[2].startsWith("N") ? Double.parseDouble(parts[2].substring(1))
					: -Double.parseDouble(parts[2].substring(1));
			double lon = parts[3].startsWith("E") ? Double.parseDouble(parts[3].substring(1))
					: -Double.parseDouble(parts[3].substring(1));

			SystemPosition pos = new SystemPosition();
			pos.imc_id = 0x8500 + id;
			pos.lat = lat;
			pos.lon = lon;
			pos.timestamp = time;

			HubSystem sys = Store.ofy().load().type(HubSystem.class).id(pos.imc_id).now();

			if (sys == null) {
				sys = new HubSystem();
				sys.setImcid(pos.imc_id);
				Address addr = Store.ofy().load().type(Address.class).id(pos.imc_id).now();
				if (addr != null)
					sys.setName(addr.name);
				else
					sys.setName("wavy-" + id);
				sys.setCreated_at(new Date());
				sys.setUpdated_at(pos.timestamp);
				sys.setCoordinates(new double[] { pos.lat, pos.lon });
			}
			sys.setUpdated_at(pos.timestamp);
			sys.setCoordinates(new double[] { pos.lat, pos.lon });
			Store.ofy().save().entity(sys);
			PositionsServlet.addPosition(pos, false);
			resp.setStatus(HttpServletResponse.SC_OK);			
			
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			e.printStackTrace(resp.getWriter());
			e.printStackTrace();
			
			resp.getWriter()
			.write("\n\nExpecting request of type " + req.getServletPath()
					+ "?v=id|HH:MM:SS|Latitude|Longitude|BattVoltage|BattPercentage|Altitude\n\n");				

			resp.getWriter().close();
			return;
		}
	}

}
