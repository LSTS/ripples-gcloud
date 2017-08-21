package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.ripples.model.SystemPosition;

public class SmsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static Pattern p = Pattern.compile("\\((.)\\) \\((.*)\\) (.*) / (.*), (.*) / .*");

	static {
		timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String phone = req.getParameter("phone");
		String text = req.getParameter("text");

		Logger.getLogger(getClass().getName()).log(Level.INFO, "Received SMS message from " + phone);
		Matcher matcher = p.matcher(text);
		if (!matcher.matches()) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "SMS message not understood: " + text);
			resp.getWriter().write("SMS message not understood: " + text);
			resp.setStatus(300);
			resp.getWriter().close();
			return;
		}
		String type = matcher.group(1);
		String vehicle = matcher.group(2);
		String timeOfDay = matcher.group(3);
		String latMins = matcher.group(4);
		String lonMins = matcher.group(5);
		GregorianCalendar date = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		String[] timeParts = timeOfDay.split(":");
		date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
		date.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
		date.set(Calendar.SECOND, Integer.parseInt(timeParts[2]));
		String latParts[] = latMins.split(" ");
		String lonParts[] = lonMins.split(" ");

		double lat = Double.parseDouble(latParts[0]);
		lat += (lat > 0) ? Double.parseDouble(latParts[1]) / 60.0 : -Double.parseDouble(latParts[1]) / 60.0;
		double lon = Double.parseDouble(lonParts[0]);
		lon += (lon > 0) ? Double.parseDouble(lonParts[1]) / 60.0 : -Double.parseDouble(lonParts[1]) / 60.0;

		int source = IMCDefinition.getInstance().getResolver().resolve(vehicle);

		if (source == -1) {
			System.err.println("Received report from unknown system name: " + vehicle);
			return;
		}
		SystemPosition position = new SystemPosition();
		position.imc_id = source;
		position.lat = lat;
		position.lon = lon;
		position.timestamp = date.getTime();
		PositionsServlet.addPosition(position, false);
		Logger.getLogger(getClass().getName()).log(Level.INFO,
				vehicle + " sent report (" + type + ") at time " + date.getTime() + ". Position: " + lat + " / " + lon);

		resp.getWriter().write("200 OK");
		resp.setStatus(200);
		resp.getWriter().close();
	}
}
