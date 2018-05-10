package pt.lsts.ripples.util;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.firebase.client.utilities.Pair;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.Credentials;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;
import pt.lsts.ripples.servlets.PositionsServlet;

public class IridiumUtils {

	public static String getIMEI(int imc_id) {
		Address addr = Store.ofy().load().type(Address.class).id(imc_id).now();
		if (addr == null)
			return null;
		return addr.imei;
	}

	public static Integer getImcId(String imei) {
		Address addr = Store.ofy().load().type(Address.class).filter("imei ==", imei).first().safe();
		if (addr == null)
			return null;
		return addr.imc_id.intValue();
	}
	
	private static Pattern p = Pattern.compile("\\((.)\\) \\((.*)\\) (.*) / (.*), (.*) / .*");
	
	public static void parsePlainTextReport(String data) throws Exception {
		try {
			data = new String(DatatypeConverter.parseHexBinary(data));	
			System.out.println("Parsing plain text: "+data);
			Matcher matcher = p.matcher(data);
			if (!matcher.matches()) {
				throw new Exception("Text message not understood: " + data);
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
			
			Date time = date.getTime();
			if (time.after(new Date(System.currentTimeMillis() + 600_000))) {
				Logger.getLogger(IridiumUtils.class.getSimpleName()).log(Level.WARNING, "Received a message from the future?");
				time = new Date(time.getTime() - 24 * 3600 * 1000);					
			}
			
			position.timestamp = time;
			System.out.println(position.timestamp+" / "+position.lat);
			PositionsServlet.addPosition(position, false);
			System.out.println(vehicle + " sent report (" + type + ") at time " + date.getTime() + ". Position: " + lat
					+ " / " + lon);
		}
		catch (Exception e) {
			Logger.getLogger(IridiumUtils.class.getSimpleName()).log(Level.WARNING, "Could not parse custom message as text", e);
		}
	}

	public static Pair<Integer, String> sendviaRockBlock(String destImei, byte[] data) throws Exception {

		Credentials cred = Store.ofy().load().type(Credentials.class).id("rockblock").now();

		if (cred == null) {
			Logger.getLogger(IridiumUtils.class.getName()).log(Level.SEVERE,
					"Could not find credentials for RockBlock. Iridium message will not be delivered.");
			return new Pair<Integer, String>(500, "Could not find credentials for RockBlock");
		}

		URL url = new URL("http://secure.rock7mobile.com/rockblock/MT");

		String content = "imei=" + URLEncoder.encode(destImei, "UTF-8");
		content += "&username=" + URLEncoder.encode(cred.login, "UTF-8");
		content += "&password=" + URLEncoder.encode(cred.password, "UTF-8");
		content += "&data=" + URLEncoder.encode(new HexBinaryAdapter().marshal(data), "UTF-8");

		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
		FetchOptions options = FetchOptions.Builder.validateCertificate();
		HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST, options);
		request.setHeader(new HTTPHeader("content-Type", "application/x-www-form-urlencoded"));
		request.setPayload(content.getBytes());
		HTTPResponse response = fetcher.fetch(request);
		return new Pair<Integer, String>(response.getResponseCode(), new String(response.getContent()));
	}

}
