package pt.lsts.ripples.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.android.gcm.server.*;


public class GCMNotification extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Put your Google API Server Key here
	private static final String GOOGLE_SERVER_KEY = "AIzaSyBi_CwWtK2VllJ-e03HRDl0Xr0e7ylgAQE";
	static final String MESSAGE_KEY = "message";
	static final String REG_ID_STORE = "GCMRegId.txt";

	public GCMNotification() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		MulticastResult result = null;

		String share = request.getParameter("shareRegId");

		// GCM RedgId of Android device to send push notification

		if (share != null && !share.isEmpty()) {
			writeToFile(request.getParameter("regId"));
			request.setAttribute("pushStatus", "GCM RegId Received.");
			request.getRequestDispatcher("push.jsp")
					.forward(request, response);
		} else {

			try {

				String userMessage = request.getParameter("message");
				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				Message message = new Message.Builder().timeToLive(30)
						.delayWhileIdle(true).addData(MESSAGE_KEY, userMessage)
						.build();
				Set<String> regIdSet = readFromFile();
				System.out.println("regId: " + regIdSet);
				List<String> regIdList = new ArrayList<String>();
				regIdList.addAll(regIdSet);
				result = sender.send(message, regIdList, 1);
				request.setAttribute("pushStatus", result.toString());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				request.setAttribute("pushStatus",
						"RegId required: " + ioe.toString());
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("pushStatus", e.toString());
			}
			request.getRequestDispatcher("push.jsp")
					.forward(request, response);
		}
	}

	private void writeToFile(String regId) throws IOException {
		Set<String> regIdSet = readFromFile();

		if (!regIdSet.contains(regId)) {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(REG_ID_STORE, true)));
			out.println(regId);
			out.close();
		}

	}

	private Set<String> readFromFile() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(REG_ID_STORE));
		String regId = "";
		Set<String> regIdSet = new HashSet<String>();
		while ((regId = br.readLine()) != null) {
			regIdSet.add(regId);
		}
		br.close();
		return regIdSet;
	}
}