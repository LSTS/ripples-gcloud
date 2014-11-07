package pt.lsts.ripples.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * @author zp
 *
 */
@Entity
@Cache
public class HubSystem {
	@Id
    long imcid;
    String name;
    String iridium;
    @Index
    Date updated_at;
    Date created_at;
    String pos_error_class;
    double[] coordinates;

    /**
     * @return the imcid
     */
    public long getImcid() {
        return imcid;
    }

    /**
     * @param imcid the imcid to set
     */
    public void setImcid(long imcid) {
        this.imcid = imcid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the iridium
     */
    public String getIridium() {
        return iridium;
    }

    /**
     * @param iridium the iridium to set
     */
    public void setIridium(String iridium) {
        this.iridium = iridium;
    }

    /**
     * @return the updated_at
     */
    public Date getUpdated_at() {
        return updated_at;
    }

    /**
     * @param updated_at the updated_at to set
     */
    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    /**
     * @return the created_at
     */
    public Date getCreated_at() {
        return created_at;
    }

    /**
     * @param created_at the created_at to set
     */
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    /**
     * @return the pos_error_class
     */
    public String getPos_error_class() {
        return pos_error_class;
    }

    /**
     * @param pos_error_class the pos_error_class to set
     */
    public void setPos_error_class(String pos_error_class) {
        this.pos_error_class = pos_error_class;
    }

    /**
     * @return the coordinates
     */
    public double[] getCoordinates() {
        return coordinates;
    }

    /**
     * @param coordinates the coordinates to set
     */
    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }
    
    @Override
    public String toString() {
        return JsonUtils.getGsonInstance().toJson(this);
    }
}
