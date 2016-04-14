package pt.lsts.ripples.servlets.datastore;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			return DataSample.parse(new HistoricData(msg));
		default:
			throw new Exception("Message type is not supported: "+msg.getAbbrev());
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		resp.setStatus(200);
		int count = Store.ofy().load().type(HistoricDatum.class).count();
		out.println("There are " + count + " samples in the cloud store.");
		out.close();

	}
}
