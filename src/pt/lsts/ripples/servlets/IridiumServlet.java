package pt.lsts.ripples.servlets;

import java.io.IOException;

import javax.servlet.http.*;

import pt.lsts.ripples.model.HubIridiumMsg;
import pt.lsts.ripples.model.JsonUtils;
import pt.lsts.ripples.model.Store;

@SuppressWarnings("serial")
public class IridiumServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write(JsonUtils.getGsonInstance().toJson(Store.ofy().load().type(HubIridiumMsg.class).list()));
		resp.getWriter().close();
	}
}
