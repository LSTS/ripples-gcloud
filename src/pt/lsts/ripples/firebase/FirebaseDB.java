package pt.lsts.ripples.firebase;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.lsts.imc.Announce;
import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.RemoteSensorInfo;
import pt.lsts.util.WGS84Utilities;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


public class FirebaseDB {

	private DataSnapshot root = null;
	private static FirebaseDB instance = null;
	private final static String rootPath = "https://neptus.firebaseio-demo.com/";
	private final static String authKey = "";
	
	private static FirebaseDB instance() {
		synchronized (FirebaseDB.class) {
			if (instance == null)
				instance = new FirebaseDB();		
			return instance;
		}		
	}
	
	public static void addListener(ValueEventListener listener, String path) {
		new Firebase(rootPath+path).addValueEventListener(listener);
	}
	
	public static void removeListener(ValueEventListener listener, String path) {
		new Firebase(rootPath+path).removeEventListener(listener);
	}
	
	public static DataSnapshot get(String path) {
		if (instance().root == null)
			return null;
		
		return instance().root.child(path);
	}
	
	public static void setMessage(IMCMessage msg) {
		switch (msg.getMgid()) {
		case Announce.ID_STATIC:
			FirebaseDB.instance().set((Announce)msg);
			break;
		case RemoteSensorInfo.ID_STATIC:
			FirebaseDB.instance().set((RemoteSensorInfo)msg);
			break;
		case EstimatedState.ID_STATIC:
			FirebaseDB.instance().set((EstimatedState)msg);
			break;
		default:
			break;
		}
	}
	
	public static void addValue(String path, Object obj) {
		new Firebase(rootPath+path).push().setValue(obj);
	}
	
	public static void setValue(String path, Object obj) {
		new Firebase(rootPath+path).setValue(obj);
	}
	
	private FirebaseDB() {
		new Firebase(rootPath).addValueEventListener(new ValueEventListener() {
			
			@Override
			public void onDataChange(DataSnapshot arg0) {
				root = arg0;				
			}
			
			@Override
			public void onCancelled(FirebaseError arg0) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Cancelled: "+arg0.getMessage());
			}
		});
		if (!authKey.isEmpty()) {
			new Firebase(rootPath).authWithCustomToken(authKey, new Firebase.AuthResultHandler() {
				@Override
				public void onAuthenticated(AuthData arg0) {
					Logger.getLogger(getClass().getName()).log(Level.INFO, "Connected.");		
				}

				@Override
				public void onAuthenticationError(FirebaseError arg0) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE, arg0.getCode()+": "+arg0.getMessage());
				}
				
			});								
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public void set(Announce ann) {
		
		double lat = Math.toDegrees(ann.getLat());
		double lon = Math.toDegrees(ann.getLon());
		if (lat == 0 && lon == 0.0)
			return;
		
		DataSnapshot posOnline = FirebaseDB.get("assets/"+ann.getSysName()+"/position");
		Map<Object, Object> data = new LinkedHashMap<Object, Object>();
		
		if (posOnline != null)
			data.putAll((Map<Object,Object>)posOnline.getValue());
		data.put("latitude", lat);
		data.put("longitude", lon);
		
		FirebaseDB.setValue("assets/"+ann.getSysName()+"/position", data);
		FirebaseDB.setValue("assets/"+ann.getSourceName()+"/updated_at", ann.getTimestampMillis());
		FirebaseDB.setValue("assets/"+ann.getSourceName()+"/type", ann.getSysType().toString());		
	}	
	
	
	@SuppressWarnings("unchecked")
	public void set(RemoteSensorInfo sinfo) {
		System.out.println("Received remote sensor info");
		double lat = Math.toDegrees(sinfo.getLat());
		double lon = Math.toDegrees(sinfo.getLon());
		if (lat == 0 && lon == 0.0)
			return;
		
		DataSnapshot posOnline = FirebaseDB.get("assets/"+sinfo.getId()+"/position");
		Map<Object, Object> data = new LinkedHashMap<Object, Object>();
		
		if (posOnline != null)
			data.putAll((Map<Object,Object>)posOnline.getValue());
		data.put("latitude", lat);
		data.put("longitude", lon);
		data.put("heading", Math.toDegrees(sinfo.getHeading()));
		data.put("altitude", Math.toDegrees(sinfo.getAlt()));

		FirebaseDB.setValue("assets/"+sinfo.getId()+"/position", data);
		FirebaseDB.setValue("assets/"+sinfo.getId()+"/updated_at", sinfo.getTimestampMillis());
		FirebaseDB.setValue("assets/"+sinfo.getId()+"/type", sinfo.getSensorClass().toString());			
	}	
	
	
	public void set(EstimatedState s) {
		double[] pos = WGS84Utilities.toLatLonDepth(s);
		LinkedHashMap<String, Object> position = new LinkedHashMap<String, Object>();
		position.put("latitude", pos[0]);
		position.put("longitude", pos[1]);
		if (pos[0] == 0 && pos[1] == 0)
			return;
		
		if (s.getAlt() != -1)
			position.put("altitude", s.getAlt());
		if (s.getDepth() != -1)
			position.put("depth", s.getDepth());
		position.put("heading", Math.toDegrees(s.getPsi()));
		position.put("speed", s.getU());
		FirebaseDB.setValue("assets/"+s.getSourceName()+"/position", position);
		FirebaseDB.setValue("assets/"+s.getSourceName()+"/updated_at", s.getTimestampMillis());
	}	
}
