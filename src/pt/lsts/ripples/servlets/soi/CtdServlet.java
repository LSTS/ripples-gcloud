package pt.lsts.ripples.servlets.soi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.gson.Gson;

import pt.lsts.ripples.model.JsonUtils;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.ctd.EnvironmentalData;

public class CtdServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ByteSource bsource = new ByteSource() {
			public InputStream openStream() throws IOException {
				return req.getInputStream();
			}
		};

		String json = bsource.asCharSource(Charsets.UTF_8).read();
		EnvironmentalData[] data = new Gson().fromJson(json, EnvironmentalData[].class);
		int count = 0;
		for (EnvironmentalData datum : data) {
			List<EnvironmentalData> existing = Store.ofy().load().type(EnvironmentalData.class)
					.filter("imc_id ==", datum.imc_id).filter("timestamp ==", datum.timestamp).list();
			if (existing.isEmpty()) {
				Store.ofy().save().entity(datum);
				count++;
			}
		}

		resp.setStatus(200);
		resp.getOutputStream().write(("Stored " + count + " messages.").getBytes());
	}

	protected void getData(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Date d = new Date(System.currentTimeMillis() - 1000 * 3600 * 24);
		List<EnvironmentalData> data = Store.ofy().load().type(EnvironmentalData.class).order("-timestamp").limit(10000)
				.list();
		resp.setContentType("application/json");
		resp.setStatus(200);
		resp.getWriter().write(JsonUtils.getGsonInstance().toJson(data));
		resp.getWriter().close();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		getData(req, resp);
	}
}
