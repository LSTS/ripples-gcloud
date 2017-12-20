package pt.lsts.ripples.util;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import com.firebase.client.Firebase;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.SoiPlan;
import pt.lsts.imc.SoiWaypoint;
import pt.lsts.ripples.model.HubSystem;

public class FirebaseUtils {

	private static Firebase _firebase = null;

	private static Firebase firebase() {
		if (_firebase == null) {
			try {
				_firebase = new Firebase("https://neptus.firebaseio.com/");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return _firebase;
	}

	public static void updateFirebase(String vehicle, SoiPlan plan) {
		Firebase planRef = firebase().child("assets/" + vehicle + "/plan").getRef();
		
		if (plan.getWaypoints().isEmpty()) {
			planRef.setValue(null);		
		}
		else {
			planRef.child("id").setValue("soi_"+plan.getPlanId());
			Vector<double[]> locs = new Vector<double[]>();
			Vector<Date> etas = new Vector<Date>();
	        for (SoiWaypoint m : plan.getWaypoints()) {
	            double lat = m.getLat();
	            double lon = m.getLon();
	            locs.add(new double[] { lat, lon});
	            etas.add(new Date(m.getEta() * 1000l));
	        }
	        planRef.child("path").setValue(locs);
	        planRef.child("eta").setValue(etas);
		}
	}
	
	public static void updateFirebase(HubSystem update) {
		Map<String, Object> assetState = new LinkedHashMap<String, Object>();
		Map<String, Object> tmp = new LinkedHashMap<String, Object>();
		tmp.put("latitude", update.getCoordinates()[0]);
		tmp.put("longitude", update.getCoordinates()[1]);
		assetState.put("position", tmp);
		assetState.put("updated_at", update.getUpdated_at().getTime());
		String typeSys = getSystemType(update.imcid);
		assetState.put("type", typeSys);
		firebase().child("assets/" + update.name).getRef().updateChildren(assetState);
	}

	public static String getSystemType(long imcId) {
		int sys_selector = 0xE000;
		int vtype_selector = 0x1C00;

		int sys_type = (int) ((imcId & sys_selector) >> 13);

		switch (sys_type) {
		case 0:
		case 1:
			switch ((int) ((imcId & vtype_selector) >> 10)) {
			case 0:
				return "UUV";
			case 1:
				return "ROV";
			case 2:
				return "USV";
			case 3:
				return "UAV";
			default:
				return "UXV";
			}
		case 2:
			return "CCU";
		default:
			break;
		}

		if (imcId > Integer.MAX_VALUE)
			return "Unknown";

		String name = IMCDefinition.getInstance().getResolver().resolve((int) imcId).toLowerCase();
		if (name.contains("ccu"))
			return "CCU";
		if (name.contains("argos"))
			return "Argos Tag";
		if (name.contains("spot"))
			return "SPOT Tag";
		if (name.contains("manta"))
			return "Gateway";
		return "Unknown";
	}

}
