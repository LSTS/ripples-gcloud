package pt.lsts.ripples.util;

import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.Credentials;
import pt.lsts.ripples.model.Store;

import com.firebase.client.utilities.Pair;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class IridiumUtils {

	public static String getIMEI(int imc_id) {
		Address addr = Store.ofy().load().type(Address.class).id(imc_id).now();
		if (addr == null)
			return null;
		return addr.imei;
	}

	public static Pair<Integer, String> sendviaRockBlock(String destImei,
			byte[] data) throws Exception {
		
		Credentials cred = Store.ofy().load().type(Credentials.class)
				.id("rockblock").now();

		if (cred == null) {
			Logger.getLogger(IridiumUtils.class.getName())
			.log(Level.SEVERE,
					"Could not find credentials for RockBlock. Iridium message will not be delivered.");
			return new Pair<Integer, String>(500, "Could not find credentials for RockBlock");
		}
		
		URL url = new URL("http://secure.rock7mobile.com/rockblock/MT");

		String content = "imei=" + URLEncoder.encode(destImei, "UTF-8");
		content += "&username=" + URLEncoder.encode(cred.login, "UTF-8");
		content += "&password=" + URLEncoder.encode(cred.password, "UTF-8");
		content += "&data="
				+ URLEncoder.encode(new HexBinaryAdapter().marshal(data),
						"UTF-8");

		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
		FetchOptions options = FetchOptions.Builder.validateCertificate();
		HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST, options);
		request.setHeader(new HTTPHeader("content-Type",
				"application/x-www-form-urlencoded"));
		request.setPayload(content.getBytes());
		HTTPResponse response = fetcher.fetch(request);
		return new Pair<Integer, String>(response.getResponseCode(),
				new String(response.getContent()));
	}

}
