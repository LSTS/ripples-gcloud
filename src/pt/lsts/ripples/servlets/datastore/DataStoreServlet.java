package pt.lsts.ripples.servlets.datastore;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pt.lsts.imc.HistoricCTD;
import pt.lsts.imc.HistoricData;
import pt.lsts.imc.HistoricEvent;
import pt.lsts.imc.HistoricTelemetry;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCInputStream;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.historic.DataSample;
import pt.lsts.ripples.model.CTDSample;
import pt.lsts.ripples.model.EventSample;
import pt.lsts.ripples.model.HistoricDatum;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.TelemetrySample;

public class DataStoreServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int MAX_RESULTS = 10;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();

		ArrayList<DataSample> samples = new ArrayList<>();

		try {
			IMCInputStream in = new IMCInputStream(req.getInputStream(), IMCDefinition.getInstance());
			while (req.getInputStream().available() > 0)
				samples.addAll(process(in.readMessage()));
			in.close();
			ArrayList<HistoricDatum> data = new ArrayList<>();
			for (DataSample sample : samples) {
				data.add(convert(sample));
			}
			Store.ofy().save().entities(data).now();
			out.println("Added " + samples.size() + " samples to cloud store.");
			resp.setStatus(200);
		} catch (Exception e) {
			e.printStackTrace(out);
			resp.setStatus(500);
		}
		out.close();
	}

	private HistoricDatum convert(DataSample sample) {
		HistoricDatum datum;
		int type = -1;
		if (sample.getSample() != null)
			type = sample.getSample().getMgid();

		switch(sample.getSample().getMgid()) {
		case HistoricCTD.ID_STATIC: {
			HistoricCTD original = new HistoricCTD(sample.getSample());
			CTDSample converted = new CTDSample();
			converted.conductivity = original.getConductivity();
			converted.temperature = original.getTemperature();
			converted.depth = original.getDepth();
			datum = converted;
			break;
		}
		case HistoricEvent.ID_STATIC: {
			HistoricEvent original = new HistoricEvent(sample.getSample());
			EventSample converted = new EventSample();
			converted.text = original.getText();
			converted.error = original.getType() == HistoricEvent.TYPE.ERROR;
			datum = converted;
			break;
		}
		case HistoricTelemetry.ID_STATIC:
			HistoricTelemetry original = new HistoricTelemetry(sample.getSample());
			TelemetrySample converted = new TelemetrySample();
			converted.altitude = original.getAltitude();
			converted.roll = (original.getRoll() / 65535.0) * 360;
			converted.pitch = (original.getPitch() / 65535.0) * 360;
			converted.yaw = (original.getYaw() / 65535.0) * 360;
			converted.speed = original.getSpeed() / 10.0;
			datum = converted;
			break;
		default:
			datum = new HistoricDatum();
			break;
		}

		datum.lat = sample.getLatDegs();
		datum.lon = sample.getLonDegs();
		datum.z = sample.getzMeters();
		datum.timestamp = new Date(sample.getTimestampMillis());
		datum.imc_id = sample.getSource();
		datum.sample_type = type;

		return datum;
	}

	private ArrayList<DataSample> process(IMCMessage msg) throws Exception {
		switch(msg.getMgid()) {
		case HistoricData.ID_STATIC: 
			return DataSample.parseSamples(new HistoricData(msg));
		default:
			throw new Exception("Message type is not supported: "+msg.getAbbrev());
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {

			listHistoricData(req, resp, MAX_RESULTS);
			resp.getWriter().close();
			return;
		} 
		else if (req.getPathInfo().equals("/search")) {
			if (parameterMatch(req,  Arrays.asList("type", "source", "since")))
				listHistoricData2(req, resp, MAX_RESULTS);
			else {
				PrintWriter out = resp.getWriter();
				printInvalid(resp, out);
			}
			resp.getWriter().close();
		}
	}

	private void printInvalid(HttpServletResponse resp, PrintWriter out) {
		resp.setContentType("text/plain");
		out.println("Invalid search parameters.");
		out.println("Example: /search?type=100&source=30&since=1460640963000");
		resp.setStatus(400);
		out.close();
	}

	private boolean parameterMatch(HttpServletRequest req, List<String> validList) {
		Map<?, ?> params = req.getParameterMap();

		if (params.keySet().size() == 0)
			return false;

		for (Object key : params.keySet()) {
			String param = (String) key;
			if (!validList.contains(param))
				return false;
		}

		return true;
	}

	private void listHistoricData2(HttpServletRequest req, HttpServletResponse resp, int limit) throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		resp.setStatus(200);
		int countTotal = Store.ofy().load().type(HistoricDatum.class).count();
		ArrayList<Filter> list = new ArrayList<>();

		String type = req.getParameter("type");
		if (type != null) {
			try {
				long typeValue = Long.parseLong(type);
				Filter typeFilter = new FilterPredicate("sample_type",
						FilterOperator.EQUAL,
						typeValue);
				list.add(typeFilter);
			} catch (NumberFormatException e) {
				printInvalid(resp, out);
				return;
			}
		}

		String system = req.getParameter("source");
		if (system != null) {
			try {
				long systemValue = Long.parseLong(system);

				Filter systemFilter = new FilterPredicate("imc_id",
						FilterOperator.EQUAL,
						systemValue);
				list.add(systemFilter);
			} catch (NumberFormatException e) {
				printInvalid(resp, out);
				return;
			}
		}

		String since = req.getParameter("since");
		if (since != null) {
			try {
				long sinceValue = Long.parseLong(since);
				DateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.ENGLISH);
				Date date = new Date(sinceValue);

				formatter.format(date);
				Filter sinceFilter = new FilterPredicate("timestamp",
						FilterOperator.GREATER_THAN_OR_EQUAL,
						date);
				list.add(sinceFilter);
			} catch (NumberFormatException e) {
				printInvalid(resp, out);
				return;
			}

		}

		Filter filter = null;
		if (list.size() == 1)
			filter = list.get(0);
		else 
			filter = CompositeFilterOperator.and(list);

		List<HistoricDatum> historicData = Store.ofy().load().type(HistoricDatum.class)
				.filter(filter)
				.order("-timestamp")
				.list();

		int count = historicData.size();

		ArrayList<HistoricDatum> entries = new ArrayList<>();

		for (HistoricDatum hd : historicData) {
			entries.add(hd);
		}
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd_HH:mm:ss").create();

		out.println(gson.toJson(entries));
		out.println("Showing " + count + " samples of "+ countTotal +" total in the cloud store...");
		out.close();

	}

	private void listHistoricData(HttpServletRequest req, HttpServletResponse resp, int limit) throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		resp.setStatus(200);

		int countTotal = Store.ofy().load().type(HistoricDatum.class).count();
		List<HistoricDatum> historicData = Store.ofy().load().type(HistoricDatum.class)
				.order("-timestamp")
				.limit(limit)
				.list();

		int count = historicData.size();

		ArrayList<HistoricDatum> entries = new ArrayList<>();

		for (HistoricDatum hd : historicData) {
			entries.add(hd);
		}
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd_HH:mm:ss").create();

		out.println(gson.toJson(entries));
		out.println("Showing " + count + " samples of "+ countTotal +" in the cloud store...");
		out.close();
	}
}
