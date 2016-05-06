package pt.lsts.ripples.servlets.commandstore;

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

import pt.lsts.imc.HistoricData;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCInputStream;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.RemoteCommand;
import pt.lsts.imc.RemoteData;
import pt.lsts.ripples.model.Command;
import pt.lsts.ripples.model.Store;

public class CommandStoreServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int MAX_RESULTS = 10;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();

		ArrayList<RemoteCommand> samples = new ArrayList<>();

		try {
			IMCInputStream in = new IMCInputStream(req.getInputStream(), IMCDefinition.getInstance());
			while (req.getInputStream().available() > 0)
				samples.addAll(process(in.readMessage()));				
			in.close();

			ArrayList<Command> data = new ArrayList<>();
			for (RemoteCommand sample : samples) {
				data.add(convert(sample));
			}
			Store.ofy().save().entities(data).now();
			out.println("Added " + samples.size() + " commands to cloud store.");
			resp.setStatus(200);
		} catch (Exception e) {
			e.printStackTrace(out);
			resp.setStatus(500);
		}
		out.close();
	}

	private Command convert(RemoteCommand sample) {
		Command ret = new Command();
		ret.command = sample.asJSON();
		ret.imc_id_dest = sample.getDst();
		ret.imc_id_source = sample.getSrc();
		ret.timeout = sample.getTimeout();
		ret.timestamp = sample.getDate();

		return ret;
	}

	private ArrayList<RemoteCommand> process(IMCMessage msg) throws Exception {
		ArrayList<RemoteCommand> ret = new ArrayList<>();
		switch(msg.getMgid()) {
		case HistoricData.ID_STATIC:
			HistoricData hist = new HistoricData(msg);
			for (RemoteData rdata : hist.getData()) {
				if (rdata instanceof RemoteCommand)
					ret.add(new RemoteCommand(rdata));
			}
			return ret;
		default:
			throw new Exception("Message type is not supported: "+msg.getAbbrev());
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {

			listStoredCommands(req, resp, MAX_RESULTS);
			resp.getWriter().close();
			return;
		} 
		else if (req.getPathInfo().equals("/search")) {
			if (parameterMatch(req,  Arrays.asList("source", "dest", "since", "timeout")))
				listStoredCommands2(req, resp, MAX_RESULTS);
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
		out.println("Example: /search?source=100&dest=30&since=1460640963000&timeout=1000");
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

	private void listStoredCommands(HttpServletRequest req, HttpServletResponse resp, int limit) throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		resp.setStatus(200);

		int countTotal = Store.ofy().load().type(Command.class).count();
		List<Command> commandsStored = Store.ofy().load().type(Command.class)
				.order("-timestamp")
				.limit(limit)
				.list();

		int count = commandsStored.size();

		ArrayList<Command> entries = new ArrayList<>();

		for (Command hd : commandsStored)
			entries.add(hd);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd_HH:mm:ss").create();

		out.println(gson.toJson(entries));
		out.println("Showing " + count + " commands of "+ countTotal +" in the cloud store...");
		out.close();
	}

	private void listStoredCommands2(HttpServletRequest req, HttpServletResponse resp, int limit) throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		resp.setStatus(200);

		int countTotal = Store.ofy().load().type(Command.class).count();
		ArrayList<Filter> list = new ArrayList<>();

		String sourceSys = req.getParameter("source");
		if (sourceSys != null) {
			try {
				long typeValue = Long.parseLong(sourceSys);
				Filter typeFilter = new FilterPredicate("imc_id_source",
						FilterOperator.EQUAL,
						typeValue);
				list.add(typeFilter);
			} catch (NumberFormatException e) {
				printInvalid(resp, out);
				return;
			}
		}

		String destSys = req.getParameter("dest");
		if (destSys != null) {
			try {
				long destSysValue = Long.parseLong(destSys);

				Filter systemFilter = new FilterPredicate("imc_id_dest",
						FilterOperator.EQUAL,
						destSysValue);
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

		String timeout = req.getParameter("timeout");
		if (timeout != null) {
			try {
				long timeoutValue = Long.parseLong(timeout);

				Filter timeoutFilter = new FilterPredicate("timeout",
						FilterOperator.EQUAL,
						timeoutValue);
				list.add(timeoutFilter);
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

		List<Command> storedCommands = Store.ofy().load().type(Command.class)
				.filter(filter)
				.order("-timestamp")
				.list();

		int count = storedCommands.size();

		ArrayList<Command> entries = new ArrayList<>();

		for (Command cmd : storedCommands) {
			entries.add(cmd);
		}
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd_HH:mm:ss").create();

		out.println(gson.toJson(entries));
		out.println("Showing " + count + " samples of "+ countTotal +" total in the cloud store...");
		out.close();

	}
}
