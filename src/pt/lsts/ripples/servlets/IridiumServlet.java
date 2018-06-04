package pt.lsts.ripples.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.firebase.client.utilities.Pair;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.IMCUtil;
import pt.lsts.imc.MessagePart;
import pt.lsts.imc.net.IMCFragmentHandler;
import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.HubIridiumMsg;
import pt.lsts.ripples.model.IridiumSubscription;
import pt.lsts.ripples.model.JsonUtils;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.iridium.ImcIridiumMessage;
import pt.lsts.ripples.model.iridium.IridiumMessage;
import pt.lsts.ripples.util.IridiumUtils;

@SuppressWarnings("serial")
public class IridiumServlet extends HttpServlet {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		boolean simulation = req.getRequestURI().contains("irsim");
		if (simulation) {
			Logger.getLogger(getClass().getName()).warning("This request is simulated.");
		}
		if ((""+req.getContentType()).equals("application/hub")) {
			sendInlineMessage(req, resp, !simulation);
		} else {
			Logger.getLogger(getClass().getName()).info(
					"Ignoring request with wrong content type.");
			resp.setStatus(400);

			resp.getWriter().close();
		}
	}

	private void sendInlineMessage(HttpServletRequest req,
			HttpServletResponse resp, boolean send) throws IOException {

		
		
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
			// store message in the DB
			HubIridiumMsg msg = new HubIridiumMsg();
			msg.setMsg(sb.toString());
			msg.setType(m.getMessageType());
			msg.setCreated_at(new Date(m.timestampMillis));
			msg.setUpdated_at(new Date());
			Store.ofy().save().entity(msg);

			Logger.getLogger(IridiumServlet.class.getSimpleName()).info("Processing " + m.getClass().getSimpleName()
					+ " from " + src + " to " + dst);
			// This message is not to be sent but just posted
			if (dst == 0 || dst == 65535) {
				IridiumMsgHandler.setMessage(null, m);
				resp.setStatus(200);
				resp.getWriter().close();
				return;
			}

			//FIXME remove
			IridiumMsgHandler.setMessage(null, m);

			if (send) {
				String imei = findImei(dst);

				if (imei == null) {
					Logger.getLogger(getClass().getName())
					.log(Level.WARNING,
							"Could not find IMEI address for dst. Iridium message will not be delivered.");
					resp.getWriter().write(
							"Could not find IMEI address for destination.");
					resp.setStatus(500);
					return;
				}

				Pair<Integer, String> rock7Resp = IridiumUtils.sendviaRockBlock(imei, m.serialize());

				if (rock7Resp.getSecond().startsWith("FAILED")) {
					Logger.getLogger(getClass().getName()).log(Level.WARNING,
							"Error sending Iridium message from " + src + " to " + dst+": "+rock7Resp.getSecond());
					resp.getWriter().write("Sending to " + imei + " failed: ");
					resp.setStatus(502);
				} else {
					resp.setStatus(rock7Resp.getFirst());
					Logger.getLogger(getClass().getName()).log(Level.INFO,
							"Sent Iridium message from " + src + " to " + dst);

					IridiumMsgHandler.setMessage(null, m);
				}
				resp.getWriter().write(rock7Resp.getSecond());
				resp.getWriter().close();
			}
			else {
				Logger.getLogger(getClass().getName()).log(Level.INFO,
						"Message has not been sent to destination (as requested)");
				resp.setStatus(200);
				resp.getWriter().write("Message has not been sent to destination (as requested)");				
				resp.getWriter().close();
			}
		}
		catch (Exception e) {
			try {
				e.printStackTrace();
				IridiumUtils.parsePlainTextReport(sb.toString()+" ("+e.getClass().getSimpleName()+" / "+e.getMessage()+")");
			}
			catch (Exception ex) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING,
						"Error sending Iridium message", e);
				resp.setStatus(400);
				e.printStackTrace(resp.getWriter());
				resp.getWriter().close();

			}
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

		if ((""+req.getPathInfo()).toUpperCase().contains("UNSUBSCRIBE")) {
			resp.setContentType("text/html");
			resp.setStatus(200);

			List<IridiumSubscription> subscribers = Store.ofy().load().type(IridiumSubscription.class).list();
			for (IridiumSubscription s : subscribers) {
				Store.ofy().delete().entity(s).now();
				Logger.getGlobal().log(Level.INFO, "Removed subscription from "+s.subscriberId);
			}

			resp.getWriter().write("UNSUBSCRIBED.");
			resp.getWriter().close();
			return;
		}

		String since = req.getParameter("since");
		String id = req.getParameter("id");
		String data = req.getParameter("data");

		if (id != null) {
			Long.parseLong(id);
			HubIridiumMsg msg = Store.ofy().load().type(HubIridiumMsg.class).id(id).now();
			if (msg == null) {
				resp.setStatus(404);
				resp.getWriter().write("No message with ID "+id);
				resp.getWriter().close();
				return;
			}
			data = msg.getMsg();
		}

		if (data != null) {
			try {
				IridiumMessage m = IridiumMessage.deserialize(new HexBinaryAdapter().unmarshal(data));
				resp.setStatus(200);
				resp.setContentType("text/html");
				for (IMCMessage message : m.asImc()) {
					resp.getWriter().write(IMCUtil.getAsHtml(message).replaceAll("<.?html>", ""));
				}
				resp.getWriter().close();
			}		
			catch (Exception e) {
				e.printStackTrace();
				resp.setStatus(500);
				resp.getWriter().write(e.getClass().getSimpleName()+": "+e.getMessage());
				resp.getWriter().close();
				return;
			}
		}
		resp.setContentType("application/json");
		Date start = new Date(System.currentTimeMillis() - 1000 * 3600 * 72);

		if (since != null) {
			try {
				start = dateFormat.parse(since);
			} catch (Exception e) {
				e.printStackTrace();
				resp.setStatus(400);
				resp.getWriter().write("Invalid date format.");
				resp.getWriter().close();
				return;
			}
		}

		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
			resp.setStatus(200);
			resp.getWriter().write(
					JsonUtils.getGsonInstance().toJson(
							Store.ofy().load().type(HubIridiumMsg.class)
							.filter("updated_at >= ", start)
							.order("-updated_at").list()));
			resp.getWriter().close();
		} else {
			try {

				if (req.getPathInfo().substring(1).equals("messages.html")) {
					resp.setContentType("text/html");
					resp.getWriter().write(table());
					resp.getWriter().close();
					return;
				}

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
				Logger.getLogger(getClass().getName()).log(Level.WARNING,
						"Error translating message to IMC", e);
				resp.setStatus(400);
				resp.getWriter().close();
			}
		}
	}

	IMCFragmentHandler handler = new IMCFragmentHandler(IMCDefinition.getInstance());

	private String table() throws Exception {
		StringBuilder sb = new StringBuilder();
		Date start = new Date(System.currentTimeMillis() - 1000 * 3600 * 72);
		List<HubIridiumMsg> msgs = Store.ofy().load().type(HubIridiumMsg.class)
				.filter("updated_at >= ", start)
				.order("updated_at").list();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		sb.append("<html>\n<body>\n<table border=1><tr><th>Updated_at</th><th>Created_at</th><th>Type</th><th>Source</th><th>Destination</th><th>Data</th></tr>\n");

		for (int i = 0; i < msgs.size(); i++) {

			HubIridiumMsg m = msgs.get(i);

			IridiumMessage msg = IridiumMessage
					.deserialize(new HexBinaryAdapter().unmarshal(m.getMsg()));

			String type = msg.getClass().getSimpleName();

			if (msg instanceof ImcIridiumMessage) {
				IMCMessage imcm = ((ImcIridiumMessage)msg).getMsg();
				if (imcm instanceof MessagePart) {
					MessagePart part = (MessagePart)imcm;

					IMCMessage result = handler.setFragment((MessagePart) imcm);
					if (result != null)
						type += " (part " + (1 + part.getFragNumber()) + "/"
								+ part.getNumFrags() + ": "
								+ result.getAbbrev() + ")";
					else
						type += " (part " + (1 + part.getFragNumber()) + "/"
								+ part.getNumFrags() + ")";
				}
				else 
					type += " ("+imcm.getAbbrev()+")";
			}
			int dst = msg.getDestination();
			int src = msg.getSource();

			String updated = sdf.format(m.getUpdated_at());
			String created = sdf.format(m.getCreated_at());
			

			String source = IMCDefinition.getInstance().getResolver().resolve(src);
			String dest = IMCDefinition.getInstance().getResolver().resolve(dst);
			String data = "<a href='"+m.getId()+"'>Data</a>";
			sb.append("<tr><td>"+updated+"</td><td>"+created+"<td>"+type+"</td><td>"+source+"</td><td>"+dest+"</td><td>"+data+"</td></tr>\n");

		}
		sb.append("</table>\n");
		List<IridiumSubscription> subscribers = Store.ofy().load().type(IridiumSubscription.class).list();
		for (IridiumSubscription s : subscribers)
			sb.append("<h2>Iridium Subscriber: "+s.imei+"</h2>\n");

		sb.append("</body></html>\n");
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		String dstr = "1f001f00da076d037d499155ab0002fd0054fe8f020401d578595f5264d541000000ffffff020f004e5f3134333535383438393333393891020700647269667465720600537572766579060090020a0063656e7465725f6c6174031200302e373138383033393034393832393937341200302e3731383830333930343938323939373490020a0063656e7465725f6c6f6e0314002d302e313531393339393035323133363838333614002d302e31353139333939303532313336383833369002040073697a6503040038302e30040038302e3090020a006c616772616e6769616e010400747275650400747275659002040070617468050600737175617265060073717561726590020700686561";
		IridiumMessage msg = IridiumMessage.deserialize(new HexBinaryAdapter()
				.unmarshal(dstr));
		System.out.println(msg.getSource());
	}
}
