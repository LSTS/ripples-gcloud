/**
 * 
 */
package pt.lsts.ripples.util;

/**
 * @author pdias
 *
 */
public class SystemUtils {

	private SystemUtils() {
	}

	public static long generateIdFromNameHash(String name) {
		long id = -1;
		int iHash = name.hashCode();
		id = (long) iHash << 32;
		return id;
	}
	
	public static void main(String[] args) {
		String[] names = { "hugin", "lauv-seacon-1", "ccu-lsts-1", "Isto é um nome grande, mesmo" };
		for (String name : names) {
			long id = generateIdFromNameHash(name);
			System.out.printf("name=%30s :: id=0x%16X  | 0x%8X\n", name, id, (id >> 32) & ~(-1L << 32));
			// System.out.printf("name=%30s :: id=%16d  | %16d\n", name, id, (id >> 32) & ~(-1L << 32));
		}
	}
}
