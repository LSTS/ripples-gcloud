package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class UpdateFirebaseServlet extends HttpServlet {


	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		if (req.getPathInfo() != null
				&& req.getPathInfo().startsWith("/update")) {
			try {
				updateFirebase();
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE,
						"Error accessing firebase API", e);
			}
		}
		resp.setContentType("text/plain");
		resp.getWriter().println("Assets updated from firebase.");
		resp.getWriter().close();
	}

	private void updateFirebase() throws Exception {
		//TODO update from https://neptus.firebaseio-demo.com/.json		
	}
}
