/**
 * 
 */
package pt.lsts.ripples.util;

import java.util.logging.Logger;

import pt.lsts.imc.RemoteSensorInfo;
import pt.lsts.imc.net.IMCProtocol;
import pt.lsts.imc.net.UDPTransport;
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
            Store.ofy().save().entity(address).now();
            Logger.getLogger(SystemUtils.class.getName()).info("Created a new address entry for " + address.name);
            addr = address;
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
		
		// 41N11'6.67'' 8W42'25.14'' 0.0
		double lat = 41.185186;
		double lon = -8.706983;
		UDPTransport udp = new UDPTransport();
		RemoteSensorInfo msg = new RemoteSensorInfo();
		msg.setSrc(22);
		msg.setId("MOV1");
		msg.setSensorClass("ship");
		msg.setLat(Math.toRadians(lat));
		msg.setLon(Math.toRadians(lon));
		int n = 0;
		while (true) {
		    n = (n+1) % 10;
            msg.setTimestampMillis(System.currentTimeMillis());
            msg.setLat(Math.toRadians(lat + n / 1E3));
            msg.setLon(Math.toRadians(lon + n / 1E3));            
            udp.sendMessage("127.0.0.1", 6001, msg);
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
	}
}
