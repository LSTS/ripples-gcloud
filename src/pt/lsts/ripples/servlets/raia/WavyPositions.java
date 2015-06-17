package pt.lsts.ripples.servlets.raia;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;
import pt.lsts.ripples.servlets.PositionsServlet;

import com.firebase.client.utilities.Pair;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class WavyPositions extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// wavies start at 0x8500

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/plain");

		//+351969173211
		String phone = req.getParameter("phone");

		//12:52:23 / N41.178697, W8.595346 / BAT: 4040mV, RAAC 6059mAh, RSAC 6112mAh, RARC 100%
		String text = req.getParameter("text");
		try {
			resp.getWriter().write("Received "+text+" from "+phone);
			
			long imc_id = findId(phone);
			
			if (imc_id == -1)
				throw new Exception("Phone number not recognized: "+phone);
			
			String[] parts1 = text.split("/");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = parts1[0];
			String coords[] = parts1[1].trim().split(", ");
			String details[] = parts1[2].trim().split(", ");
			
			sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
			sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
			
			String date = sdf1.format(new Date())+" "+time;
			Date d = sdf2.parse(date);
			double lat = Double.parseDouble(coords[0].substring(1)),
				   lon = Double.parseDouble(coords[1].substring(1));
				   
			int voltage = Integer.parseInt(details[0].replaceAll("BAT: ", "").replaceAll("mV", ""));
			int percent = Integer.parseInt(details[3].replaceAll("RARC ", "").replaceAll("%", ""));

			if (coords[0].startsWith("S"))
				lat = -lat;
			if (coords[1].startsWith("W"))
				lon = -lon;
			
			SystemPosition pos = new SystemPosition();
			pos.imc_id = imc_id;
			pos.lat = lat;
			pos.lon = lon;
			pos.timestamp = d;
			
			HubSystem sys = Store.ofy().load().type(HubSystem.class).id(pos.imc_id).now();
			
			if (sys == null) {
				sys = new HubSystem();
				sys.setImcid(pos.imc_id);
				Address addr = Store.ofy().load().type(Address.class).id(pos.imc_id).now();
				if (addr != null)
					sys.setName(addr.name);
				else
					sys.setName("wavy-"+(0x8500-pos.imc_id));
				sys.setCreated_at(new Date());
				sys.setUpdated_at(pos.timestamp);
				sys.setCoordinates(new double[] { pos.lat, pos.lon });
			}
			sys.setUpdated_at(pos.timestamp);
			sys.setCoordinates(new double[] { pos.lat, pos.lon });
			Store.ofy().save().entity(sys);
			PositionsServlet.addPosition(pos);
			
			Logger.getLogger(getClass().getName()).log(Level.INFO, "Date: "+d+", lat: "+lat+", lon: "+lon+", voltage: "+voltage+", percent: "+percent+", ID: "+findId(phone));
			resp.getWriter().write("\nDate: "+d+", lat: "+lat+", lon: "+lon+", voltage: "+voltage+", percent: "+percent+", ID: "+findId(phone)+"\n");
			
			Pair<Integer, String> res = forward(imc_id - 0x8500, time.trim(), coords[0], coords[1], ""+voltage, ""+percent);
			
			resp.setStatus(res.getFirst());
			resp.getWriter().write("\nFORWARD RESULT:"+res.getSecond());
			resp.getWriter().close();		
		}
		catch (Exception e) {
			resp.setStatus(405);
			resp.getWriter().write("Invalid request: "+req.getQueryString()+"\n\n");
			e.printStackTrace(resp.getWriter());
			e.printStackTrace();
			resp.getWriter().close();
			return;
		}
	}
	
	private Pair<Integer, String> forward(long id, String time, String lat, String lon, String bat, String percent) {
		try {
			String url = "http://wavy.inesctec.pt:80/u?v="+URLEncoder.encode(id+"|"+time+"|"+lat+"|"+lon+"|"+bat+"|"+percent, "UTF-8");
			HTTPRequest request = new HTTPRequest(new URL(url), HTTPMethod.GET);
			URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
			HTTPResponse response = fetcher.fetch(request);
			return new Pair<Integer, String>(response.getResponseCode(),
					new String(response.getContent()));
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<Integer, String>(500, e.getClass().getSimpleName()
					+ ": " + e.getMessage());			
		}
	}


	private long findId(String phone) {
		Address addr = Store.ofy().load().type(Address.class).filter("phone =", phone).first().now();

		if (addr != null)
			return addr.imc_id;
		return -1;
	}
}
