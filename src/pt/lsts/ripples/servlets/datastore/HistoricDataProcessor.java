package pt.lsts.ripples.servlets.datastore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.google.appengine.api.datastore.Blob;

import pt.lsts.imc.HistoricCTD;
import pt.lsts.imc.HistoricData;
import pt.lsts.imc.HistoricEvent;
import pt.lsts.imc.HistoricTelemetry;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.RemoteCommand;
import pt.lsts.imc.historic.DataSample;
import pt.lsts.ripples.model.CTDSample;
import pt.lsts.ripples.model.Command;
import pt.lsts.ripples.model.DataRoute;
import pt.lsts.ripples.model.EventSample;
import pt.lsts.ripples.model.HistoricDatum;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.TelemetrySample;

public class HistoricDataProcessor {

	public static void processData(HistoricData msg) throws Exception {
		ArrayList<DataSample> samples = new ArrayList<>();
		
		samples.addAll(process(msg));
		ArrayList<Command> cmds = extractCommands(msg);
		
		ArrayList<HistoricDatum> data = new ArrayList<>();
		for (DataSample sample : samples)
			data.add(convert(sample));
		
		HashSet<Integer> differentSources = new HashSet<>();
		for (HistoricDatum d : data) {
			if (d.imc_id != msg.getSrc())
				differentSources.add((int)d.imc_id);
		}
		
		for (Command c : cmds) {
			if (c.imc_id_source != msg.getSrc())
				differentSources.add((int)c.imc_id_source);
		}
		
		for (int sys : differentSources)
			addRoute(sys, msg.getSrc());
		
		Store.ofy().save().entities(data).now();
		Store.ofy().save().entities(cmds).now();

		System.out.println("Added " + samples.size() + " samples and " + cmds.size() + " commands from "
				+ msg.getSourceName() + " to cloud store.");
	}
	
	private static HistoricDatum convert(DataSample sample) {
		HistoricDatum datum;
		int type = -1;
		if (sample.getSample() != null)
			type = sample.getSample().getMgid();

		switch (sample.getSample().getMgid()) {
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

	private static ArrayList<DataSample> process(IMCMessage msg) throws Exception {
		switch (msg.getMgid()) {
		case HistoricData.ID_STATIC:
			return DataSample.parseSamples(new HistoricData(msg));
		default:
			throw new Exception("Message type is not supported: " + msg.getAbbrev());
		}
	}

	private static Command convert(RemoteCommand sample) {
		Command ret = new Command();
		ret.cmd = new Blob(sample.getCmd().toByteArray());
		ret.imc_id_dest = sample.getDestination();
		ret.imc_id_source = sample.getOriginalSource();
		ret.timeout = sample.getTimeout();
		ret.timestamp = sample.getDate();
		return ret;
	}

	private static ArrayList<Command> extractCommands(IMCMessage msg) throws Exception {
		ArrayList<Command> ret = new ArrayList<>();
		switch (msg.getMgid()) {
		case HistoricData.ID_STATIC:
			HistoricData hist = new HistoricData(msg);
			for (RemoteCommand rcmd : DataSample.parseCommands(hist)) {
				if (rcmd.getTimeout() * 1000 > System.currentTimeMillis())
					ret.add(convert(rcmd));
				else
					System.out.println("Discarding expired command: " + ret);
			}
			return ret;
		default:
			throw new Exception("Message type is not supported: " + msg.getAbbrev());
		}
	}
	
	private static void addRoute(int system, int gateway) {
		DataRoute route = null;
		List<DataRoute> routes = Store.ofy().load().type(DataRoute.class).
				filter("gateway", (long)gateway).filter("system", (long)system).list();
		
		if (routes.isEmpty()) {
			route = new DataRoute();
			route.gateway = gateway;
			route.system = system;
			route.timestamp = System.currentTimeMillis();			
		}
		else {
			route = routes.get(0);
			route.timestamp = System.currentTimeMillis();
		}
		
		Store.ofy().save().entity(route);
	}
	
}
