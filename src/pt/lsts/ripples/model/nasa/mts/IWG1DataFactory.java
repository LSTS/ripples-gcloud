/**
 * 
 */
package pt.lsts.ripples.model.nasa.mts;

import pt.lsts.ripples.model.HubSystem;
import pt.lsts.ripples.model.SystemPosition;

/**
 * @author pdias
 *
 */
public class IWG1DataFactory {

    private IWG1DataFactory() {
    }

    public static IWG1Data create(HubSystem hSystem) {
        IWG1Data data = new IWG1Data();
        data.setTimeStampMillis(hSystem.getUpdated_at().getTime());
        data.setLatitudeDegs(hSystem.getCoordinates()[0]);
        data.setLatitudeDegs(hSystem.getCoordinates()[1]);
        data.setSourceId(hSystem.getImcid());
        return data;
    }

    public static IWG1Data create(SystemPosition p) {
        IWG1Data data = new IWG1Data();
        data.setTimeStampMillis(p.timestamp.getTime());
        data.setLatitudeDegs(p.lat);
        data.setLatitudeDegs(p.lon);
        data.setSourceId(p.imc_id);
        return data;
    }
}
