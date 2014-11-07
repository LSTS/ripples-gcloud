package pt.lsts.ripples.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.NameValuePair;
import org.shaded.apache.http.client.HttpClient;
import org.shaded.apache.http.client.entity.UrlEncodedFormEntity;
import org.shaded.apache.http.client.methods.HttpPost;
import org.shaded.apache.http.impl.client.HttpClientBuilder;
import org.shaded.apache.http.message.BasicNameValuePair;

import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.Credentials;
import pt.lsts.ripples.model.HubIridiumMsg;
import pt.lsts.ripples.model.JsonUtils;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.iridium.IridiumMessage;

@SuppressWarnings("serial")
public class IridiumServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (req.getContentType().equals("application/hub")) {
			sendInlineMessage(req, resp);
		} else {
			Logger.getGlobal()
					.info("Ignoring request with wrong content type.");
			resp.setStatus(400);
			resp.getWriter().close();
		}
	}

	public static String sendToRockBlockHttp(String destImei, String username,
			String password, byte[] data) throws Exception {

		HttpClient client = HttpClientBuilder.create().build();

		HttpPost post = new HttpPost(
				"https://secure.rock7mobile.com/rockblock/MT");
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("imei", destImei));
		urlParameters.add(new BasicNameValuePair("username", username));
		urlParameters.add(new BasicNameValuePair("password", password));
		urlParameters.add(new BasicNameValuePair("data", new HexBinaryAdapter()
				.marshal(data)));

		post.setEntity(new UrlEncodedFormEntity(urlParameters));
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		HttpResponse response = client.execute(post);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}

	private void sendInlineMessage(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {

		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(
				req.getInputStream()));
		String line = r.readLine();

		while (line != null) {
			sb.append(line.trim());
			line = r.readLine();
		}

		try {
			IridiumMessage m = IridiumMessage
					.deserialize(new HexBinaryAdapter().unmarshal(sb.toString()));
			int dst = m.getDestination();
			int src = m.getSource();

			String imei = findImei(dst);

			if (imei == null) {
				Logger.getGlobal()
						.log(Level.SEVERE,
								"Could not find IMEI address for dst. Iridium message will not be delivered.");
				resp.getWriter().write(
						"Could not find IMEI address for destination.");
				resp.setStatus(500);
				return;
			}

			Credentials cred = Store.ofy().load().type(Credentials.class)
					.id("rockblock").now();

			if (cred == null) {
				Logger.getGlobal()
						.log(Level.SEVERE,
								"Could not find credentials for RockBlock. Iridium message will not be delivered.");
				resp.getWriter().write(
						"Credentials for RockBlock service have not been set.");
				resp.setStatus(500);
				return;
			}

			sendToRockBlockHttp(imei, cred.login, cred.password, m.serialize());
			Logger.getGlobal().log(Level.INFO,
					"Sent Iridium message from " + src + " to " + dst);

		} catch (Exception e) {
			Logger.getGlobal().log(Level.WARNING,
					"Error sending Iridium message", e);
			resp.setStatus(400);
			resp.getWriter().close();
			return;
		}
	}

	private String findImei(int imcid) {
		Address addr = Store.ofy().load().type(Address.class).id(imcid).now();
		if (addr == null)
			return null;
		return addr.imei;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/json");

		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
			resp.setStatus(200);
			resp.getWriter().write(
					JsonUtils.getGsonInstance()
							.toJson(Store.ofy().load()
									.type(HubIridiumMsg.class).list()));
			resp.getWriter().close();
		} else {
			try {
				Long l = Long.parseLong(req.getPathInfo().substring(1));
				HubIridiumMsg msg = Store.ofy().load()
						.type(HubIridiumMsg.class).id(l).now();
				if (msg == null) {
					resp.setStatus(404);
					resp.getWriter().close();
				} else {
					resp.setStatus(200);
					IridiumMessage m = IridiumMessage
							.deserialize(new HexBinaryAdapter().unmarshal(msg
									.getMsg()));
					resp.getWriter().write("" + m.asImc());
					resp.getWriter().close();
				}
			} catch (Exception e) {
				Logger.getGlobal().log(Level.WARNING,
						"Error translating message to IMC", e);
				resp.setStatus(400);
				resp.getWriter().close();
			}
		}
	}
}
