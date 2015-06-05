package pt.lsts.ripples.servlets;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.cmd.LoadType;
import com.googlecode.objectify.cmd.Query;

import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.JsonUtils;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.model.SystemPosition;
import pt.lsts.ripples.model.nasa.mts.IWG1Data;
import pt.lsts.ripples.model.nasa.mts.IWG1DataFactory;

@SuppressWarnings("serial")
public class PositionsIwg1Servlet extends HttpServlet {

	// http://hub.lsts.pt/api/v1/csvTag/<day>
	private static final SimpleDateFormat dayFormat = new SimpleDateFormat(
			"yyyy-MM-dd") {{ setTimeZone(TimeZone.getTimeZone("UTC")); }};
	private static DateFormat timeFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss") {{ setTimeZone(TimeZone.getTimeZone("UTC")); }};

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		try {
		    String systemStr = null;
		    String pInfo = req.getPathInfo();
		    String[] splitLst = pInfo == null ? new String[0] : req.getPathInfo().split("[/\\.]+");
            String day;
		    if (splitLst.length < 1) {
		        day = dayFormat.format(new Date(System.currentTimeMillis()));
	            listSystems(req, resp);
	            return;
		    }
		    else {
		        String dayOrSystem = splitLst[1];
		        try {
                    Date date = dayFormat.parse(dayOrSystem);
                    // All systems for a date
                    getSystemByNameForDate(null, date.getTime(), req, resp);
                    return;
                }
                catch (ParseException e) {
                    // Let us try if system exist
                    Date date = null;
                    systemStr = splitLst[1];
                    if (splitLst.length > 2) {
                        dayOrSystem = splitLst[2];
                        try {
                            date = dayFormat.parse(dayOrSystem);
                            getSystemByNameForDate(systemStr, date.getTime(), req, resp);
                            return;
                        }
                        catch (ParseException e1) {
                            getSystemByName(systemStr, req, resp);
                            return;
                        }
                    }
                    else {
                        getSystemByName(systemStr, req, resp);
                        return;
                    }
                }
		    }

//		    resp.setContentType("text/plain");
//            resp.setStatus(200);
//            resp.getWriter().close();
//		    return;
		    
		    
//			long start = dayFormat.parse(day).getTime();
//			long nextMidnight = start + 3600 * 24 * 1000;
//			List<SystemPosition> positions = Store.ofy().load()
//					.type(SystemPosition.class).filter("timestamp >=", new Date(start))
//					.filter("timestamp <=", new Date(nextMidnight))
//					.list();
//
//			resp.setContentType("text/plain");
//
//			for (SystemPosition p : positions) {
//				resp.getWriter().write(
//						String.format("%s, %d, %.8f, %.8f\n",
//								timeFormat.format(p.timestamp), p.imc_id,
//								p.lat, p.lon));
//			}
//			resp.getWriter().close();

		}
		catch (Exception e) {
			resp.sendError(400, "Bad date format, expected YYYY-MM-DD.");
			return;
		}
	}

	private long getId(String assetName) {
	    Address addr = Store.ofy().load().type(Address.class).filter("name", assetName).first().now();
	    if (addr == null)
	        return -1;      
	    return addr.imc_id;
	}

    private void getSystemByNameForDate(String system, long start, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        try {
            long nextMidnight = start + 3600 * 24 * 1000;
            Query<SystemPosition> positionsLT = Store.ofy().load()
                    .type(SystemPosition.class).filter("timestamp >=", new Date(start))
                    .filter("timestamp <=", new Date(nextMidnight));
            List<SystemPosition> positions = positionsLT.order("-timestamp").list();

            long id = -1;
            if (system != null && !"all".equalsIgnoreCase(system)
                    && !"".equalsIgnoreCase(system)) {
                try {
                    id = Long.parseLong(system);
                }
                catch (NumberFormatException e) {
                    try {
                        id = Long.parseLong(system.replace("0x", "").replace("0X", ""), 16);
                    }
                    catch (NumberFormatException e1) {
                    }
                }
            }
            if (id != -1) {
                positions = positionsLT.filter("imc_id", id).order("-timestamp").list();
            }
            else if (system != null && !"all".equalsIgnoreCase(system)
                    && !"".equalsIgnoreCase(system)) {
                id = getId(system);
                positions = positionsLT.filter("imc_id", id).order("-timestamp").list();
            }

            ArrayList<IWG1Data> data = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (SystemPosition p : positions) {
                IWG1Data iwg1 = IWG1DataFactory.create(p);
                data.add(iwg1);
                sb.append(iwg1.toIWG1());
            }
            resp.setContentType("text/plain");
            resp.setStatus(200);
            resp.getWriter().write(sb.toString());
            resp.getWriter().close();
        }
        catch (Exception e) {
            return;
        }
    }

    /**
     * @param system
     * @param req
     * @param resp
     * @throws IOException
     */
    private void getSystemByName(String system, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        
        if (system.equals("active"))
            listActiveSystems(req, resp);
        else {
            try {
                LoadType<HubSystem> elemsLT = Store.ofy().load().type(HubSystem.class);
                List<HubSystem> elems = elemsLT.order("-updated_at").list();
                if (elems == null) {
                    resp.setStatus(404);
                }
                else {
                    long id = -1;
                    if (system != null && !"all".equalsIgnoreCase(system)
                            && !"".equalsIgnoreCase(system)) {
                        try {
                            id = Long.parseLong(system);
                        }
                        catch (NumberFormatException e) {
                            try {
                                id = Long.parseLong(system.replace("0x", "").replace("0X", ""), 16);
                            }
                            catch (NumberFormatException e1) {
                            }
                        }
                    }
//                    if (id != -1) {
//                        resp.getWriter().write("B" + elems.size() + "  " + system + ":" + id);
//                        elems = elemsLT.filter("imcid", id).order("-updated_at").list();
//                        resp.getWriter().write("A" + elems.size());
//                    }
//                    else if (id == -1 && system != null && !"all".equalsIgnoreCase(system)
//                            && !"".equalsIgnoreCase(system)) {
//                        resp.getWriter().write("B" + elems.size() + "  " + system + ":" + id);
//                        elems = elemsLT.filter("name", system).order("-updated_at").list();
//                        resp.getWriter().write("A" + elems.size());
//                    }
                    
                    ArrayList<IWG1Data> data = new ArrayList<>();
                    StringBuilder sb = new StringBuilder();
                    for (HubSystem hubSystem : elems) {
                        if (id != -1 && hubSystem.getImcid() != id) {
                            continue;
                        }
                        else if (id == -1 && system != null && !"all".equalsIgnoreCase(system)
                                && !"".equalsIgnoreCase(system) 
                                && !system.equalsIgnoreCase(hubSystem.getName())) {
                            continue;
                        }
                        IWG1Data iwg1 = IWG1DataFactory.create(hubSystem);
                        data.add(iwg1);
                        sb.append(iwg1.toIWG1());
                    }
                    resp.setContentType("text/plain");
                    resp.setStatus(200);
                    resp.getWriter().write(sb.toString());
                    resp.getWriter().close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error handling " + req.getPathInfo(), e);
                resp.setStatus(400);
            }
        }
        resp.getWriter().close();
    }
    
    /**
     * Retrieve a specific system (provided on the path)
     */
    private void getSystem(String system, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        if (system.equals("active"))
            listActiveSystems(req, resp);
        else {
            try {
                long id = Long.parseLong(system);
                HubSystem s = Store.ofy().load().type(HubSystem.class).id(id).now();
                if (s == null) {
                    resp.setStatus(404);
                }
                else {
                    IWG1Data iwg1 = IWG1DataFactory.create(s);
                    resp.setContentType("text/plain");
                    resp.setStatus(200);
                    resp.getWriter().write(iwg1.toIWG1());
                    resp.getWriter().close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error handling " + req.getPathInfo(), e);
                resp.setStatus(400);
            }
        }
        resp.getWriter().close();
    }

    /**
     * Retrieve only systems that were updated on the last 3 days
     */
    public void listActiveSystems(HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        Date d = new Date(System.currentTimeMillis() - 1000 * 3600 * 24);
        List<HubSystem> systems = Store.ofy().load().type(HubSystem.class)
                .filter("updated_at >=", d).order("-updated_at").list();
        resp.setContentType("application/json");
        resp.setStatus(200);
        resp.getWriter().write(JsonUtils.getGsonInstance().toJson(systems));
        resp.getWriter().close();
    }
    
	/**
     * Retrieve all stored systems (new and old)
     */
    public void listSystems(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        List<HubSystem> systems = Store.ofy().load().type(HubSystem.class)
                .order("-updated_at").list();
        resp.setContentType("text/plain");
        
        ArrayList<IWG1Data> data = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (HubSystem hubSystem : systems) {
            IWG1Data iwg1 = IWG1DataFactory.create(hubSystem);
            data.add(iwg1);
            sb.append(iwg1.toIWG1());
        }
        resp.setStatus(200);
        resp.getWriter().write(sb.toString());
        resp.getWriter().close();
    }

}
