package pt.lsts.ripples.model.log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.github.rjeschke.txtmark.Processor;
import com.google.gson.Gson;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Cache
public class MissionLog {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
	
	@Id
	public String date = sdf.format(new Date());
	
	public String place = "";
	public String conditions = "";
	public ArrayList<String> objectives = new ArrayList<>();
	public ArrayList<String> team = new ArrayList<>();
	public ArrayList<String> systems = new ArrayList<>();
	public ArrayList<LogEntry> log = new ArrayList<>();
	public ArrayList<ActionItem> actions = new ArrayList<>();
	
	
	public String asMarkDown() {
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
		hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		StringBuilder buf = new StringBuilder();
		buf.append("Mission Report\n");
		buf.append("================\n\n");
		
		buf.append("Date\n");
		buf.append("----\n\n");
		buf.append(date.trim()+"\n\n");
		
		buf.append("Place\n");
		buf.append("-----\n\n");
		buf.append(place.trim()+"\n\n");
		
		buf.append("Team\n");
		buf.append("----\n\n");
		for (String teamMember : team)
			buf.append("* " + teamMember.trim()+"\n");
		
		buf.append("Systems\n");
		buf.append("-------\n\n");
		for (String system : systems)
			buf.append("* " + system.trim()+"\n");
		buf.append("\n");
		
		buf.append("Conditions\n");
		buf.append("----------\n\n");
		buf.append(conditions.trim()+"\n");
		
		buf.append("\n");
		buf.append("Timezone\n");
		buf.append("--------\n\nUTC\n\n");
		
		buf.append("Objectives\n");
		buf.append("----------\n\n");
		for (String objective : objectives)
			buf.append("* " + objective.trim()+"\n");
		buf.append("\n");
		
		buf.append("Log Book\n");
		buf.append("--------\n\n");
		
		for (LogEntry entry : log) {
			String date = "*"+hourFormat.format(entry.timestamp)+"*";
			if (entry.text.trim().contains("\n"))
				buf.append("* "+date+" \n>"+entry.text.trim()+"\n");
			else
				buf.append("* "+date+" "+entry.text.trim()+"\n");
		}
		buf.append("\n");
		buf.append("Action Items\n");
		buf.append("------------\n");
		
		LinkedHashMap<String, ArrayList<ActionItem>> itemsPerModule = new LinkedHashMap<>();
		for (ActionItem item : actions) {
			String moduleNormalized = item.module.trim().toLowerCase(); 
			if (!itemsPerModule.containsKey(moduleNormalized))
				itemsPerModule.put(moduleNormalized, new ArrayList<ActionItem>());
			itemsPerModule.get(moduleNormalized).add(item);
		}
		
		for (Entry<String, ArrayList<ActionItem>> action : itemsPerModule.entrySet()) {
			buf.append("* "+action.getValue().get(0).module+":\n");
			for (ActionItem item : action.getValue()) {
				buf.append("   * "+item.text.trim()+"\n");
			}
		}
		
		return buf.toString();
	}
	
	public String asHtml() {
		return Processor.process(asMarkDown());
	}
	
	public String asJson() {
		return new Gson().toJson(this);
	}
	
	public static void main(String[] args) {
		MissionLog log = new MissionLog();
		log.conditions = "clear sky.";
		log.place = "APDL";
		log.objectives.add("Test noptilus-2");
		log.systems.add("lauv-noptilus-2");
		LogEntry entry = new LogEntry();
		ActionItem item1 = new ActionItem();
		item1.module = "DUNE";
		item1.text = "Fix configuration of lauv-noptilus-1";
		log.actions.add(item1);
		ActionItem item2 = new ActionItem();
		item2.module = "DUNe";
		item2.text = "Fix configuration of lauv-noptilus-4";
		log.actions.add(item2);
		entry.author = "ZP";
		entry.text = "oops... sometyhing wrong happened.";
		log.log.add(entry);
		
		System.out.println(log.asHtml());
		
		System.out.println(log.asMarkDown());
		
		System.out.println(log.asJson());
	}	
}
