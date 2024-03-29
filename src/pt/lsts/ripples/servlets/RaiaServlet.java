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

public class RaiaServlet extends HttpServlet {

	private static final long serialVersionUID = -5715124502844646874L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain");
		//resp.getWriter().write(req.getParameter("id"));
		//resp.getWriter().write(req.getParameter("ip"));
		
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
		//load
		
		List<BuoyAddress> buoyData = Store.ofy().load().type(BuoyAddress.class).list();
		
		//resp.setContentType("text/plain");
		//resp.getWriter().println(d.ip+" : "+d.id);
		
		out.println("<!DOCTYPE html><html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\" />");


        out.println("<title> RAIA </title>");
        out.println("<style> #test {margin-top: 50px; margin-left: auto; margin-right: auto; width: 70%; text-align: center;} "
        		  + "table, th, td { margin: 0 auto; border: 1px solid black; border-collapse: collapse; border-spacing: 50px;} td { padding: 10px;} </style>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");

        // note that all links are created to be relative. this
        // ensures that we can move the web application that this
        // servlet belongs to to a different place in the url
        // tree and not have any harmful side effects.

        // XXX
        // making these absolute till we work out the
        // addition of a PathInfo issue

        out.println("<div id='test'>");

        out.println("<h2>Buoy Data</h2>");
        out.println("<table>");
        out.println("<tr>");
        out.println("<td>ID</td>");
        out.println("<td>IP</td>");
        out.println("<td>Battery (%)</td>");
        out.println("<td>Date</td>");
        out.println("</tr>");
        
        for (BuoyAddress data : buoyData) {
        	out.println("<tr>");
        	out.println("<td>"+data.id+"</td>");
        	out.println("<td>"+data.ip+"</td>");
        	out.println("<td>"+data.battery+"</td>");
        	out.println("<td>"+data.date+"</td>");
        	out.println("</tr>");
        }
        
        out.println("</table>");
        
        out.println("</div>");
        
        out.println("</body>");
        out.println("</html>");
		
	}
	
}
