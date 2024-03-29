package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.log.ActionItem;
import pt.lsts.ripples.model.log.LogEntry;
import pt.lsts.ripples.model.log.MissionLog;

public class LogbookServlet extends HttpServlet {
	private static final long serialVersionUID = 307618046379130819L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (!req.getContentType().startsWith("application/json")) {
			resp.sendError(400, "Invalid content type: " + req.getContentType()
					+ ". Only JSON is allowed.");
			return;
		}
		
		MissionLog log = getLogBook(req);

		if (log == null) {
			resp.sendError(400, "Logbook does not exist.");
			return;
		}
		
		if (req.getPathInfo().split("/").length > 2) {
			String member = req.getPathInfo().split("/")[2].toLowerCase();
			int indexToDelete = -1;
			if (req.getPathInfo().split("/").length > 4
					&& req.getPathInfo().split("/")[3].equals("delete")) {
				try {
					indexToDelete = Integer.parseInt(req.getPathInfo().split(
							"/")[4]);
				} catch (Exception e) {
					resp.sendError(500, e.getMessage());
				}
			}

			req.setCharacterEncoding("UTF-8");
			
			switch (member) {
			case "place":
				try {
					log.place = new JsonParser().parse(req.getReader())
							.getAsString();
				} catch (Exception e) {
					resp.sendError(400,
							"Bad data. Expecting JSON String element.");
				}
				break;
			case "conditions":
				try {
					log.conditions = new JsonParser().parse(req.getReader())
							.getAsString();
				} catch (Exception e) {
					resp.sendError(400,
							"Bad data. Expecting JSON String element.");
				}
				break;
			case "systems":
				if (indexToDelete >= 0) {
					try {
						log.systems.remove(log.systems.toArray()[indexToDelete]);
					}
					catch (Exception e) {
						resp.sendError(400,
								"No such index.");
					}
				} else {
					JsonElement elem = new JsonParser().parse(req.getReader());
					if (elem.isJsonPrimitive()) {
						String[] elems = new String[] {elem.getAsString()};
						if (elems[0].contains(","))
							elems = elems[0].split("[, ]+");
						for (String e : elems)
							log.systems.add(e);						
					} else {
						resp.sendError(400,
								"Bad data. Expecting JSON String element.");
						return;
					}
				}
				break;
			case "team":
				if (indexToDelete >= 0) {
					try {
						log.team.remove(log.team.toArray()[indexToDelete]);
					}
					catch (Exception e) {
						resp.sendError(400,
								"No such index.");
					}

				} else {
					JsonElement elem = new JsonParser().parse(req.getReader());
					if (elem.isJsonPrimitive()){
						String[] elems = new String[] {elem.getAsString()};
						if (elems[0].contains(","))
							elems = elems[0].split(",");
						for (String e : elems)
							log.team.add(e.trim());	
					}						
					else {
						resp.sendError(400,
								"Bad data. Expecting JSON String element.");
						return;
					}
				}
				break;
			case "objectives":
				if (indexToDelete >= 0) {
					try {
						log.objectives.remove(log.objectives.toArray()[indexToDelete]);
					}
					catch (Exception e) {
						resp.sendError(400,
								"No such index.");
					}

				} else {
					JsonElement elem = new JsonParser().parse(req.getReader());
					if (elem.isJsonPrimitive()) {
						String[] elems = new String[] {elem.getAsString()};
						if (elems[0].contains(","))
							elems = elems[0].split("[, ]+");
						for (String e : elems)
							log.objectives.add(e);	
					}
					else {
						resp.sendError(400,
								"Bad data. Expecting JSON String element.");
						return;
					}
				}
				break;
			case "actions":
				if (indexToDelete >= 0) {
					try {
						log.actions.remove(log.actions.toArray()[indexToDelete]);
					}
					catch (Exception e) {
						resp.sendError(400,
								"No such index.");
					}

				} else {
					JsonElement elem = new JsonParser().parse(req.getReader());

					try {
						ActionItem item = new ActionItem();
						item.module = elem.getAsJsonObject().get("module")
								.getAsString();
						item.text = elem.getAsJsonObject().get("text")
								.getAsString();
						log.actions.add(item);
					} catch (Exception e) {
						resp.sendError(400,
								"Bad data. Expecting JSON Object element with fields 'module' and 'text'");
						return;
					}
				}
				break;
			case "log":
				if (indexToDelete >= 0) {
					try {
						log.log.remove(log.log.toArray()[indexToDelete]);
					}
					catch (Exception e) {
						resp.sendError(400,
								"No such index.");
					}

				} else {
					JsonElement elem = new JsonParser().parse(req.getReader());
					try {
						LogEntry entry = new LogEntry();
						entry.author = elem.getAsJsonObject().get("author")
								.getAsString();
						entry.text = elem.getAsJsonObject().get("text")
								.getAsString();
						if (elem.getAsJsonObject().get("dataUrl") != null) {
							entry.dataUrl = elem.getAsJsonObject()
									.get("dataUrl").getAsString();
						}
						if (elem.getAsJsonObject().get("tags") != null) {
							JsonArray arr = elem.getAsJsonObject().get("tags")
									.getAsJsonArray();
							for (JsonElement el : arr)
								entry.tags.add(el.getAsString());
						}
						log.log.add(entry);
					} catch (Exception e) {
						resp.sendError(
								400,
								"Bad data. Expecting JSON Object element with fields 'author' 'text', 'dataUrl', tags[]");
						return;
					}
				}
				break;
			default:
				resp.sendError(400, "Member " + member
						+ " does not exist in logbook.");
				return;
			}
		}

		Store.ofy().save().entity(log);

		resp.setContentType("application/json");
		resp.getWriter().write(log.asJson());
		resp.getWriter().close();

	}

	private MissionLog getLogBook(HttpServletRequest req) {
		String[] pathParts = req.getPathInfo().split("[/\\.]+");
		try {
			return Store.ofy().load().type(MissionLog.class).id(pathParts[1])
					.now();
		} catch (Exception e) {
			return null;
		}
	}

	private String getExtension(HttpServletRequest req) {
		String[] pathParts = req.getPathInfo().split("/");

		if (pathParts.length < 2)
			return null;
		try {
			return pathParts[1].toLowerCase().split("\\.")[1];
		} catch (Exception e) {
			return null;
		}

	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	/**
	 * Parse the URL path and respond accordingly
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
			listLogs(req, resp);
			resp.getWriter().close();
			return;
		}
		else if (req.getPathInfo().equals("/rt")) {
			MissionLog log = new MissionLog();
			MissionLog existing = Store.ofy().load().type(MissionLog.class)
					.id(log.date).now();
			
				
			ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
			if (existing != null) {
				for (LogEntry le : existing.log) {
					if (System.currentTimeMillis() - le.timestamp < 60000) {
						entries.add(le);
					}
				}
			}
			resp.setContentType("application/json");
			resp.getWriter().write(new Gson().toJson(entries));
			resp.getWriter().close();
			return;
		} 
		else if (req.getPathInfo().split("/").length >= 2) {
			String[] pathParts = req.getPathInfo().split("[/\\.]+");

			if (pathParts[1].equals("create")) {
				MissionLog log = new MissionLog();
				MissionLog existing = Store.ofy().load().type(MissionLog.class)
						.id(log.date).now();
				if (existing == null) {
					Store.ofy().save().entity(log).now();
				}
				resp.sendRedirect(req.getServletPath() + "/" + log.date);
				return;
			}

			MissionLog log = getLogBook(req);
			if (log == null) {
				resp.sendError(404, "The requested log book does not exist.");
				return;
			} else {
				String extension = getExtension(req);
				if (extension == null)
					extension = "html";
				
				switch (extension) {
				case "html":
					resp.setContentType("text/html; charset=utf-8");
					resp.setCharacterEncoding("UTF-8");
					resp.getOutputStream().write(log.asHtml().getBytes(Charset.forName("UTF-8")));
					resp.getOutputStream().close();
					break;
				case "md":
					resp.setContentType("text/plain; charset=utf-8");
					resp.setCharacterEncoding("UTF-8");
					resp.getWriter().write(log.asMarkDown());
					resp.getWriter().close();
					break;
				default:
					resp.setContentType("application/json; charset=utf-8");
					resp.setCharacterEncoding("UTF-8");
					resp.getWriter().write(log.asJson());
					resp.getWriter().close();
					break;
				}
			}
		}
	}

	public void listLogs(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		List<MissionLog> logs = Store.ofy().load().type(MissionLog.class)
				.list();
		resp.getWriter().write("<html>\n");
		resp.getWriter().write("<h1>Mission log books</h1>\n");
		resp.getWriter().write("<ul>\n");
		for (MissionLog l : logs) {
			resp.getWriter().write(
					"<li><a href=\"" + req.getServletPath() + "/" + l.date
							+ "\">" + l.date + "</li>\n");
		}
		resp.getWriter().write("</ul>\n");
	}
}
