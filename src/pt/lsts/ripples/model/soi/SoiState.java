package pt.lsts.ripples.model.soi;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import pt.lsts.endurance.Asset;
import pt.lsts.endurance.AssetState;
import pt.lsts.endurance.Plan;
import pt.lsts.imc.SoiPlan;
import pt.lsts.imc.StateReport;
import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.Store;
import pt.lsts.ripples.servlets.IridiumMsgHandler;
import pt.lsts.ripples.util.FirebaseUtils;

@Entity
@Cache
@Index
public class SoiState {

	@Id
	public String name;

	// Last update to this state
	public Date lastUpdated = null;

	// The json representation of the state
	public String asset = null;

	public static void updateState(StateReport msg) {
		HubSystem vehicle = Store.ofy().load().type(HubSystem.class).id(msg.getSrc()).now();

		SoiState existing = Store.ofy().load().type(SoiState.class).id(vehicle.getName()).now();
		Asset state = new Asset(vehicle.getName());
		if (existing == null) {
			existing = new SoiState();
			existing.name = vehicle.getName();											
		}
		else {
			try {
				state = Asset.parse(existing.asset);	
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		state.setState(AssetState.builder()
				.withLatitude(msg.getLatitude())
				.withLongitude(msg.getLongitude())
				.withTimestamp(msg.getDate())
				.withHeading(msg.getHeading() / (65535 * 360.0))
				.withFuel(msg.getFuel()/255.0)				
				.build());
		
		existing.asset = state.toString();
		existing.lastUpdated = msg.getDate();
		Store.ofy().save().entities(existing).now();
		
		Logger.getLogger(SoiState.class.getName()).log(Level.INFO,
				"Received report for " + vehicle + ": " + msg);
	}

	public static void updatePlan(HubSystem vehicle, SoiPlan plan) {
		SoiState existing = Store.ofy().load().type(SoiState.class).id(vehicle.getName()).now();

		FirebaseUtils.updateFirebase(vehicle.getName(), plan);

		Logger.getLogger(SoiState.class.getName()).log(Level.INFO,
				"Received plan for " + vehicle + ": " + plan);

		if (existing == null) {
			existing = new SoiState();
			existing.lastUpdated = plan.getDate();
			existing.name = vehicle.getName();

			Asset state = new Asset(vehicle.getName());

			state.setState(AssetState.builder().withLatitude(vehicle.getCoordinates()[0])
					.withLongitude(vehicle.getCoordinates()[1]).withTimestamp(plan.getDate()).build());

			System.out.println("New state: "+state.toString());
			
			existing.asset = state.toString();
			
		}

		try {
			Asset state = Asset.parse(existing.asset);
			if (plan == null)
				state.setPlan(null);
			else
				state.setPlan(Plan.parse(plan.asJSON()));

			existing.asset = state.toString();
			existing.lastUpdated = plan.getDate();
			System.out.println("Plan's date: "+plan.getDate());
			Store.ofy().save().entity(existing).now();
			Logger.getLogger(IridiumMsgHandler.class.getName()).info("Saved SoiPlan for vehicle " + vehicle.getName());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(IridiumMsgHandler.class.getName())
					.warning("Error saving SoiPlan for vehicle " + vehicle.getName() + ": " + e.getMessage());
		}

	}
}
