package pt.lsts.ripples.servlets.datastore;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCInputStream;
import pt.lsts.imc.IMCMessage;

public class DataStoreServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	IMCMessage message = null;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setContentType("text/plain");
		
		
		
	    PrintWriter out = resp.getWriter();

	    out.println("Request Headers:");
	    out.println();
	    Enumeration<?> names = req.getHeaderNames();
	    while (names.hasMoreElements()) {
	      String name = (String) names.nextElement();
	      Enumeration<?> values = req.getHeaders(name);  // support multiple values
	      if (values != null) {
	        while (values.hasMoreElements()) {
	          String value = (String) values.nextElement();
	          out.println("\t"+name + ": " + value);
	        }
	      }
	    }
	    
	    out.println("\n\nBody:");
	    out.println("\tContent length: "+req.getContentLength());
	    try {
	    	out.println("\n\nMessage:");
	    	IMCInputStream in = new IMCInputStream(req.getInputStream(), IMCDefinition.getInstance());
			IMCMessage msg = in.readMessage();
			out.print(msg.asJSON(true));
			in.close();
			message = msg;
			resp.setStatus(200);
	    }
	    catch (Exception e) {
	    	e.printStackTrace(out);
	    	resp.setStatus(500);
	    }	
	    out.close();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();

		out.println("Request Headers:");
	    out.println();
	    Enumeration<?> names = req.getHeaderNames();
	    while (names.hasMoreElements()) {
	      String name = (String) names.nextElement();
	      Enumeration<?> values = req.getHeaders(name);  // support multiple values
	      if (values != null) {
	        while (values.hasMoreElements()) {
	          String value = (String) values.nextElement();
	          out.println("\t"+name + ": " + value);
	        }
	      }
	    }
	    
		out.println("\n\nMessage:");
		if (message != null)
			out.print(message.asJSON(true));
		else
			out.print("\nNULL\n");
		resp.setStatus(200);
		out.close();
		
	}	
}
