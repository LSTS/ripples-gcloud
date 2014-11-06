package pt.lsts.ripples.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

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
		}
		else {
			Logger.getGlobal().info("Ignoring request with wrong content type.");
			resp.setStatus(400);
			resp.getWriter().close();
		}
	}

	private void sendInlineMessage(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(req.getInputStream()));
		String line = r.readLine();

		while (line != null) {
			sb.append(line.trim());
			line = r.readLine();
		}

		try {
			IridiumMessage m = IridiumMessage.deserialize(new HexBinaryAdapter().unmarshal(sb.toString()));
			int dst = m.getDestination();
			int src = m.getSource();

			Credentials cred =  Store.ofy().load().type(Credentials.class).id("rockblock").now();

			if (cred == null) {
				Logger.getGlobal()
				.log(Level.SEVERE,
						"Could not find credentials for RockBlock. Iridium message will not be delivered.");
				resp.getWriter()
				.write("Credentials for RockBlock service have not been set. Please contact the system administrator");
				resp.setStatus(500);
				return;
			}

			System.out.println("Message from "+src+" to "+dst+": "+m);
			//TODO
		}
		catch (Exception e) {
			Logger.getGlobal().log(Level.WARNING, "Error parsing message to send", e);
			resp.setStatus(400);
			resp.getWriter().close();
			return;
		}
	}



	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/json");

		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
			resp.setStatus(200);
			resp.getWriter().write(JsonUtils.getGsonInstance().toJson(Store.ofy().load().type(HubIridiumMsg.class).list()));
			resp.getWriter().close();	
		}
		else {
			try {
				Long l = Long.parseLong(req.getPathInfo().substring(1));
				HubIridiumMsg msg = Store.ofy().load().type(HubIridiumMsg.class).id(l).now();
				if (msg == null) {
					resp.setStatus(404);
					resp.getWriter().close();
				}
				else {
					resp.setStatus(200);
					IridiumMessage m = IridiumMessage.deserialize(new HexBinaryAdapter().unmarshal(msg.getMsg()));
					resp.getWriter().write(""+m.asImc());
					resp.getWriter().close();
				}					
			}
			catch (Exception e) {
				Logger.getGlobal().log(Level.WARNING, "Error translating message to IMC", e);
				resp.setStatus(400);
				resp.getWriter().close();
			}
		}
	}
}
