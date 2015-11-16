package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.ripples.model.BuoyAddress;
import pt.lsts.ripples.model.Store;

public class Raia2Servlet extends HttpServlet {

	private static final long serialVersionUID = -5715124502844646874L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain");
		// resp.getWriter().write(req.getParameter("id"));
		// resp.getWriter().write(req.getParameter("ip"));

		BuoyAddress data = new BuoyAddress();
		data.id = req.getParameter("id");
		data.ip = req.getParameter("ip");
		data.battery = req.getParameter("battery");
		data.date = req.getParameter("date");

		Store.ofy().save().entity(data).now();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		// load

		List<BuoyAddress> buoyData = Store.ofy().load().type(BuoyAddress.class)
				.list();

		// resp.setContentType("text/plain");
		// resp.getWriter().println(d.ip+" : "+d.id);

		out.println("<!DOCTYPE html><html>");
		out.println("<head>");
		out.println("<meta charset=\"UTF-8\" />");

		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");

		out.println("<title> RAIA v2</title>");

		out.println("<link rel=\"stylesheet\" href=\"../bootstrap/dist/css/bootstrap.min.css\">");
		out.println("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>");
		out.println("<script src=\"../bootstrap/dist/js/bootstrap.min.js\"></script>");
		out.println("<style>#wrapper {border: 1px #e4e4e4 solid;margin-top: 3%;padding: 2%;border-radius: 4px;box-shadow: 0 0 6px #ccc;background-color: #fff;min-width: 300px; width:55%} @media only screen and (max-width: 360px) {table .table-hover tr td{float:left;width:240px;height:100px;list-style: none;}}</style>");
		out.println("</head>");
		out.println("<body bgcolor=\"white\">");

		// note that all links are created to be relative. this
		// ensures that we can move the web application that this
		// servlet belongs to to a different place in the url
		// tree and not have any harmful side effects.

		// XXX
		// making these absolute till we work out the
		// addition of a PathInfo issue

		out.println("<script> $(function() {$('#buoy-table a:first').tab('show'); }) </script>");

		out.println("<div id=\"wrapper\" class=\"container\">");

		out.println("<h2>Buoy Data</h2>");

		out.println("<ul class=\"nav nav-tabs\" id=\"buoy-table\">");
		out.println("<li><a href=\"#1\" data-toggle=\"tab\">Buoy 1</a></li>");
		out.println("<li><a href=\"#2\" data-toggle=\"tab\">Dummy A</a></li>");
		out.println("<li><a href=\"#3\" data-toggle=\"tab\">Dummy B</a></li>");
		out.println("</ul>");

		out.println("<div class=\"tab-content\">");
		out.println("<div class=\"tab-pane\" id=\"1\">");
		for (BuoyAddress data : buoyData) {
		out.println("<table class=\"table\">");
		out.println("<tr>");
		out.println("<td>");
		out.println("<h3>Overview</h3>");
		
		out.println("<table class=\"table table-hover\">");
		out.println("<tr>");
		out.println("<td width=\"12%\">System: </td>");
		out.println("<td>"+data.id+"</td>");
		out.println("<td width=\"12%\">Position: </td>");
		out.println("<td>N41 / W8</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>Date: </td>");
		out.println("<td>"+data.date+"</td>");
		out.println("<td>Software version: </td>");
		out.println("<td>v001</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>Uptime: </td>");
		out.println("<td>8 hours, 47 minutes and 14 seconds </td>");
		out.println("<td>Power: </td>");
		out.println("<td>"+data.battery+"</td>");
		out.println("</tr>");
		out.println("</table>");
		
		/*for (BuoyAddress data : buoyData) {
			out.println("<tr>");
			out.println("<td>" + data.id + "</td>");
			out.println("<td>" + data.ip + "</td>");
			out.println("<td>" + data.battery + "</td>");
			out.println("<td>" + data.date + "</td>");
			out.println("</tr>");
		}*/

		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>");
		out.println("<h3>Data</h3>");
		
		out.println("<table class=\"table table-hover\">");
		out.println("<tr>");
		out.println("<td width=\"12%\">IP: </td>");
		out.println("<td>"+data.ip+"</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>Sensor list: </td>");
		out.println("<td >data 1</td>");
		out.println("</tr>");
		out.println("</table>");
		
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		}
		out.println("</div>");//class=\"tab-pane\" id=\"1\"
		
		out.println("<div class=\"tab-pane\" id=\"2\">");
		out.println("<table class=\"table\">");
		out.println("<tr>");
		out.println("<td>");
		out.println("<h3>Overview</h3>");
		
		out.println("<table class=\"table table-hover\">");
		out.println("<tr>");
		out.println("<td width=\"12%\">System: </td>");
		out.println("<td>raia buoy A</td>");
		out.println("<td width=\"12%\">Position: </td>");
		out.println("<td>N41 / W8</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>Date: </td>");
		out.println("<td>current</td>");
		out.println("<td>Software version: </td>");
		out.println("<td>v001</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>Uptime: </td>");
		out.println("<td>8 hours, 47 minutes and 14 seconds </td>");
		out.println("<td>Power: </td>");
		out.println("<td>100%</td>");
		out.println("</tr>");
		out.println("</table>");
		
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>");
		out.println("<h3>Data</h3>");
		
		out.println("<table class=\"table table-hover\">");
		out.println("<tr>");
		out.println("<td width=\"12%\">IP: </td>");
		out.println("<td >127.0.0.0</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>Sensor list: </td>");
		out.println("<td >data A</td>");
		out.println("</tr>");
		out.println("</table>");
		
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</div>");//class=\"tab-pane\" id=\"2\"
		
		out.println("<div class=\"tab-pane\" id=\"3\">");
		out.println("<table class=\"table\">");
		out.println("<tr>");
		out.println("<td>");
		out.println("<h3>Overview</h3>");
		
		out.println("<table class=\"table table-hover\">");
		out.println("<tr>");
		out.println("<td width=\"12%\">System: </td>");
		out.println("<td>raia buoy B</td>");
		out.println("<td width=\"12%\">Position: </td>");
		out.println("<td>N41 / W8</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>Date: </td>");
		out.println("<td>current</td>");
		out.println("<td>Software version: </td>");
		out.println("<td>v002</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>Uptime: </td>");
		out.println("<td>8 hours, 47 minutes and 14 seconds </td>");
		out.println("<td>Power: </td>");
		out.println("<td>100%</td>");
		out.println("</tr>");
		out.println("</table>");
		
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>");
		out.println("<h3>Data</h3>");
		
		out.println("<table class=\"table table-hover\">");
		out.println("<tr>");
		out.println("<td width=\"12%\">IP: </td>");
		out.println("<td >127.0.0.0</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td>Sensor list: </td>");
		out.println("<td >data B</td>");
		out.println("</tr>");
		out.println("</table>");
		
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</div>");//class=\"tab-pane\" id=\"3\"

		out.println("</div>");//class=\"tab-content\"

		out.println("</div>");//class=\"container\"

		out.println("</body>");
		out.println("</html>");

	}

}
