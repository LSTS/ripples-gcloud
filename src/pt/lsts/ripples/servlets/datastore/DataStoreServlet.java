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

import pt.lsts.imc.HistoricCTD;
import pt.lsts.imc.HistoricData;
import pt.lsts.imc.HistoricEvent;
import pt.lsts.imc.HistoricEvent.TYPE;
import pt.lsts.imc.HistoricTelemetry;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCInputStream;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.IMCOutputStream;
import pt.lsts.imc.IMCUtil;
import pt.lsts.imc.historic.DataSample;
import pt.lsts.imc.historic.DataStore;
import pt.lsts.ripples.model.CTDSample;
import pt.lsts.ripples.model.EventSample;
import pt.lsts.ripples.model.HistoricDatum;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.TelemetrySample;

public class DataStoreServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int MAX_RESULTS = 500;

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

	private DataSample convert(HistoricDatum datum) {
		DataSample sample = new DataSample();
		sample.setLatDegs(datum.lat);
		sample.setLonDegs(datum.lon);
		sample.setzMeters(datum.z);
		sample.setSource((int)datum.imc_id);
		sample.setTimestampMillis(datum.timestamp.getTime());

		if (datum instanceof CTDSample) {
			CTDSample ctdDatum = (CTDSample)datum;
			HistoricCTD ctd = new HistoricCTD();
			ctd.setConductivity(ctdDatum.conductivity);
			ctd.setDepth(ctdDatum.depth);
			ctd.setTemperature(ctdDatum.temperature);
			sample.setSample(ctd);
		}
		else if (datum instanceof TelemetrySample) {
			TelemetrySample telDatum = (TelemetrySample)datum;
			HistoricTelemetry tel = new HistoricTelemetry();
			tel.setRoll((int)(telDatum.roll * 65535 / 360));
			tel.setPitch((int)(telDatum.pitch * 65535 / 360));
			tel.setYaw((int)(telDatum.yaw * 65535 / 360));
			tel.setSpeed((short) (telDatum.speed * 10));
			sample.setSample(tel);
		}
		else if (datum instanceof EventSample) {
			EventSample evtDatum = (EventSample) datum;
			HistoricEvent evt = new HistoricEvent();
			evt.setText(evtDatum.text);
			evt.setType(evtDatum.error? TYPE.ERROR : TYPE.INFO);
			sample.setSample(evt);
		}
		
		return sample;
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

		String type = "lsf";
		
		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
			listHistoricData(req, resp, MAX_RESULTS, type);
			resp.getWriter().close();
			return;
		}
		
		if (!req.getParameterNames().hasMoreElements()) {
			if (req.getPathInfo().equalsIgnoreCase("/json"))
				listHistoricData(req, resp, MAX_RESULTS, "json");		
			else if (req.getPathInfo().equalsIgnoreCase("/lsf"))
				listHistoricData(req, resp, MAX_RESULTS, "lsf");
			else if (req.getPathInfo().equalsIgnoreCase("/xml"))
				listHistoricData(req, resp, MAX_RESULTS, "xml");
			else {
				PrintWriter out = resp.getWriter();
				printInvalid(resp, out);
				resp.getWriter().close();
			}
			return;
		}
					
		if (!parameterMatch(req,  Arrays.asList("type", "source", "since"))) {
			PrintWriter out = resp.getWriter();
			printInvalid(resp, out);
			resp.getWriter().close();
			return;
		}
		
		if (req.getPathInfo().equalsIgnoreCase("/json"))
			filterHistoricData(req, resp, MAX_RESULTS, "json");		
		else if (req.getPathInfo().equalsIgnoreCase("/imc"))
			filterHistoricData(req, resp, MAX_RESULTS, "imc");
		else if (req.getPathInfo().equalsIgnoreCase("/xml"))
			filterHistoricData(req, resp, MAX_RESULTS, "xml");
		else if (req.getPathInfo().equalsIgnoreCase("/html"))
			filterHistoricData(req, resp, MAX_RESULTS, "html");
	}

	private void printInvalid(HttpServletResponse resp, PrintWriter out) {
		resp.setContentType("text/plain");
		out.println("Invalid request. Examples:");
		out.println("/json?type=100&source=30&since="+(System.currentTimeMillis()/1000-3600));
		out.println("/xml");
		out.println("/lsf&since="+(System.currentTimeMillis()/1000-3600));
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

	private void filterHistoricData(HttpServletRequest req, HttpServletResponse resp, int limit, String contenttype) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.setStatus(200);
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
				printInvalid(resp, resp.getWriter());
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
				printInvalid(resp, resp.getWriter());
				return;
			}
		}
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		String since = req.getParameter("since");
		if (since == null)
			since = ""+(System.currentTimeMillis()-3600*1000)/1000;

		try {
			long sinceValue = Long.parseLong(since);

			Date date = new Date(sinceValue);

			formatter.format(date);
			Filter sinceFilter = new FilterPredicate("timestamp",
					FilterOperator.GREATER_THAN_OR_EQUAL,
					date);
			list.add(sinceFilter);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			printInvalid(resp, resp.getWriter());
			return;
		}

		Filter filter = null;
		if (list.size() == 1)
			filter = list.get(0);
		else 
			filter = CompositeFilterOperator.and(list);

		List<HistoricDatum> historicData = Store.ofy().load().type(HistoricDatum.class)
				.filter(filter)
				.order("-timestamp")
				.limit(MAX_RESULTS)
				.list();			

		DataStore store = new DataStore();
		for (HistoricDatum datum : historicData)
			store.addSample(convert(datum));
		HistoricData data;
		try {
			 data = store.pollData(0, 65000);
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
		
		switch (type) {
		case "json":
			resp.setContentType("application/json");
			resp.getWriter().println(data.asJSON(true));
			resp.getWriter().close();
			break;
		case "xml":
			resp.setContentType("application/xml");
			resp.getWriter().println(data.asXml(false));
			resp.getWriter().close();
			break;
		case "lsf":
			resp.setContentType("application/lsf");
			IMCOutputStream ios = new IMCOutputStream(resp.getOutputStream());
			ios.writeMessage(data);
			resp.getOutputStream().close();
			break;
		default:
			resp.setContentType("application/html");
			resp.getWriter().println(IMCUtil.getAsHtml(data));
			resp.getWriter().close();
			break;
		}
	}

	private void listHistoricData(HttpServletRequest req, HttpServletResponse resp, int limit, String type) throws ServletException, IOException {

		List<HistoricDatum> historicData = Store.ofy().load().type(HistoricDatum.class)
				.order("-timestamp")
				.limit(limit)
				.list();

		resp.setStatus(200);

		DataStore store = new DataStore();
		for (HistoricDatum datum : historicData)
			store.addSample(convert(datum));
		HistoricData data;
		try {
			 data = store.pollData(0, 65000);
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
		
		switch (type) {
		case "json":
			resp.setContentType("application/json");
			resp.getWriter().println(data.asJSON(true));
			resp.getWriter().close();
			break;
		case "xml":
			resp.setContentType("application/xml");
			resp.getWriter().println(data.asXml(false));
			resp.getWriter().close();
			break;
		case "html":
			resp.setContentType("application/html");
			resp.getWriter().println(IMCUtil.getAsHtml(data));
			resp.getWriter().close();
			break;
		default:
			resp.setContentType("application/lsf");
			IMCOutputStream ios = new IMCOutputStream(resp.getOutputStream());
			ios.writeMessage(data);
			resp.getOutputStream().close();
			break;		
		}		
	}
}
