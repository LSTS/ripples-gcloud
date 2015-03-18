package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;

public class PositionsCsvServlet extends HttpServlet {

	private static final long serialVersionUID = -1332271427836127406L;
	// http://hub.lsts.pt/api/v1/csvTag/<day>
	private static final SimpleDateFormat dayFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static DateFormat timeFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	static {
		dayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		try {
			String day = req.getPathInfo().split("[/\\.]+")[1];
			long start = dayFormat.parse(day).getTime();
			long nextMidnight = start + 3600 * 24 * 1000;
			List<SystemPosition> positions = Store.ofy().load()
					.type(SystemPosition.class).filter("timestamp >=", new Date(start))
					.filter("timestamp <=", new Date(nextMidnight))
					.list();

			resp.setContentType("text/plain");

			for (SystemPosition p : positions) {
				resp.getWriter().write(
						String.format("%s, %d, %.8f, %.8f\n",
								timeFormat.format(p.timestamp), p.imc_id,
								p.lat, p.lon));
			}
			resp.getWriter().close();

		} catch (Exception e) {
			resp.sendError(400, "Bad date format, expected YYYY-MM-DD.");
			return;
		}
	}
}
