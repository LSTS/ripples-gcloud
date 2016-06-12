package pt.lsts.ripples.servlets.datastore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import pt.lsts.imc.HistoricCTD;
import pt.lsts.imc.HistoricData;
import pt.lsts.imc.HistoricEvent;
import pt.lsts.imc.HistoricEvent.TYPE;
import pt.lsts.imc.HistoricSample;
import pt.lsts.imc.HistoricTelemetry;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCInputStream;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.IMCOutputStream;
import pt.lsts.imc.IMCUtil;
import pt.lsts.imc.RemoteCommand;
import pt.lsts.imc.RemoteData;
import pt.lsts.imc.historic.DataSample;
import pt.lsts.imc.historic.DataStore;
import pt.lsts.ripples.model.CTDSample;
import pt.lsts.ripples.model.Command;
import pt.lsts.ripples.model.EventSample;
import pt.lsts.ripples.model.HistoricDatum;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.TelemetrySample;

public class DataStoreServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int MAX_RESULTS = 500;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setContentType("application/lsf");	
		
		ArrayList<DataSample> samples = new ArrayList<>();

		try {
			resp.setStatus(200);
			IMCInputStream in = new IMCInputStream(req.getInputStream(), IMCDefinition.getInstance());
			IMCMessage msg = in.readMessage();
			in.close();
			
			samples.addAll(process(msg));
			
			ArrayList<Command> cmds = extractCommands(msg);
			
			ArrayList<HistoricDatum> data = new ArrayList<>();
			for (DataSample sample : samples)
				data.add(convert(sample));
			Store.ofy().save().entities(data).now();
			Store.ofy().save().entities(cmds).now();
			
			System.out.println("Added " + samples.size() + " samples and "+cmds.size()+" commands from "+msg.getSourceName()+" to cloud store.");
			
			IMCMessage m = dataFor(msg.getSrc());
			IMCOutputStream ios = new IMCOutputStream(resp.getOutputStream());
			ios.writeMessage(m);
			resp.getOutputStream().close();
			
		} catch (Exception e) {
			resp.setStatus(500);	
			PrintWriter out = resp.getWriter();
			e.printStackTrace(out);
			out.close();				
		}
	}
	
	private IMCMessage dataFor(int dst) {
		HistoricData data = new HistoricData();
		data.setDst(dst);
		Vector<RemoteData> cmds = data.getData();
		for (Command cmd : Store.ofy().load().type(Command.class).filter("imc_id_dest==", dst).iterable()) {
			try {
				cmds.add(convert(cmd));
			}
			catch (Exception e) {
				e.printStackTrace();
			}			
		}
		data.setData(cmds);
		return data;
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
		else {
			System.err.println("Unrecongnized type of datum: "+datum.getClass().getSimpleName());
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
	
	private Command convert(RemoteCommand sample) {
		Command ret = new Command();
		ret.cmd = new Blob(sample.toByteArray());
		ret.imc_id_dest = sample.getDst();
		ret.imc_id_source = sample.getSrc();
		ret.timeout = sample.getTimeout();
		ret.timestamp = sample.getDate();
		return ret;
	}
	
	private RemoteCommand convert(Command cmd) throws IOException {
		RemoteCommand ret = new RemoteCommand();
		IMCInputStream iis = new IMCInputStream(new ByteArrayInputStream(cmd.cmd.getBytes()), IMCDefinition.getInstance());
		ret.setCmd(iis.readMessage());
		iis.close();
		ret.setDestination((int)cmd.imc_id_dest);
		ret.setTimeout(cmd.timeout);
		ret.setTimestamp(cmd.timestamp.getTime()/1000.0);
		return ret;
	}

	private ArrayList<Command> extractCommands(IMCMessage msg) throws Exception {
		ArrayList<Command> ret = new ArrayList<>();
		switch(msg.getMgid()) {
		case HistoricData.ID_STATIC:
			HistoricData hist = new HistoricData(msg);
			for (RemoteCommand rcmd : DataSample.parseCommands(hist)) {
				if (rcmd.getTimeout() * 1000 > System.currentTimeMillis())
					ret.add(convert(rcmd));
				else
					System.out.println("Discarding expired command: "+ret);
			}
			return ret;
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
			else if (req.getPathInfo().equalsIgnoreCase("/html"))
				listHistoricData(req, resp, MAX_RESULTS, "html");
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
		else if (req.getPathInfo().equalsIgnoreCase("/lsf"))
			filterHistoricData(req, resp, MAX_RESULTS, "lsf");
	}

	private void printInvalid(HttpServletResponse resp, PrintWriter out) {
		resp.setContentType("text/plain");
		out.println("Invalid request. Examples:");
		out.println("/json?type=100&source=30&since="+(System.currentTimeMillis()-3600*1000));
		out.println("/xml");
		out.println("/lsf&since="+(System.currentTimeMillis()-3600*1000));
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
		String since = req.getParameter("since");
		if (since == null)
			since = ""+(System.currentTimeMillis()-3600*1000)/1000;

		try {
			long sinceValue = Long.parseLong(since);
			Date date = new Date(sinceValue);
			Filter sinceFilter = new FilterPredicate("timestamp",
					FilterOperator.GREATER_THAN,
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
				.order("timestamp")
				.limit(MAX_RESULTS)
				.list();			

		DataStore store = new DataStore();
		HistoricData data;
		
		if (historicData.isEmpty()) {
			HistoricDatum lastSample = Store.ofy().load().type(HistoricDatum.class).order("-timestamp").first().now();
			data = new HistoricData();
			data.setBaseTime(System.currentTimeMillis() / 1000);
			if (lastSample != null)
				data.setBaseTime(lastSample.timestamp.getTime() / 1000);
		}
		else {
			for (HistoricDatum datum : historicData) {
				if (datum.getClass() != HistoricDatum.class)
					store.addSample(convert(datum));
			}				
			try {
				data = store.pollData(0, 65000);
			}
			catch (Exception e) {
				throw new ServletException(e);
			}
		}
		
		serveHistoricData(data, contenttype, resp);
	}

	private void listHistoricData(HttpServletRequest req, HttpServletResponse resp, int limit, String type) throws ServletException, IOException {

		List<HistoricDatum> historicData = Store.ofy().load().type(HistoricDatum.class)
				.order("-timestamp")
				.limit(limit)
				.list();

		resp.setStatus(200);

		DataStore store = new DataStore();
		HistoricData data;
		
		if (historicData.isEmpty()) {
			data = new HistoricData();
			data.setBaseTime(System.currentTimeMillis()/1000);
		}
		else {
			for (HistoricDatum datum : historicData)
				if (datum.getClass() != HistoricDatum.class)
					store.addSample(convert(datum));
			try {
				data = store.pollData(0, 65000);
			}
			catch (Exception e) {
				throw new ServletException(e);
			}
		}
		
		serveHistoricData(data, type, resp);	
	}
	
	private void serveHistoricData(HistoricData data, String contentType, HttpServletResponse resp) throws IOException {
		switch (contentType) {
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
			resp.setContentType("text/html");
			resp.getWriter().println("<h3>Number of samples: "+data.getData().size()+"</h3>");
			resp.getWriter().println("<h3>Message size: "+data.getPayloadSize()+"</h3>");
			resp.getWriter().println("<h3>Last time: "+new Date((long)(data.getBaseTime()*1000))+"</h3>");
			int t = 0;
			if (!data.getData().isEmpty())
				t = ((HistoricSample)data.getData().lastElement()).getT();
			resp.getWriter().println("<h3>Initial time: "+new Date((long)((data.getBaseTime()+t)*1000))+"</h3>");
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
