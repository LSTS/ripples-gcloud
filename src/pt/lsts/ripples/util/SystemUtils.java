/**
 * 
 */
package pt.lsts.ripples.util;

import java.util.logging.Logger;

import pt.lsts.ripples.model.Address;
import pt.lsts.ripples.model.Store;

/**
 * @author pdias
 *
 */
public class SystemUtils {

	private SystemUtils() {
	}

	public static long generateId(String name) {
		long id = -1;
		int iHash = name.hashCode();
		id = (long) iHash << 32;
		return id;
	}

	public static long getOrGuessId(String assetName) {
	    Address addr = Store.ofy().load().type(Address.class).filter("name", assetName).first().now();
	    long id;
	    if (addr == null) {
	        id = SystemUtils.generateId(assetName);
            Address address = new Address();
            address.imc_id = id;
            address.name = assetName;
            Store.ofy().save().entity(address);
            Logger.getLogger(SystemUtils.class.getName()).info("Created a new address entry for " + address.name);	        
	    }
	    else
	    	id = addr.imc_id;
	    
	    System.out.println("ID for asset "+assetName+" guessed to be "+id);
	    
	    return addr.imc_id;
	}

	public static void main(String[] args) {
		String[] names = { "hugin", "lauv-seacon-1", "ccu-lsts-1", "Isto é um nome grande, mesmo", "poseidon" };
		for (String name : names) {
			long id = generateId(name);
			System.out.printf("name=%30s :: id=0x%16X  | 0x%8X\n", name, id, (id >> 32) & ~(-1L << 32));
			System.out.printf("name=%30s :: id=%16d  | %16d\n", name, id, (id >> 32) & ~(-1L << 32));
		}
	}
}
