package pt.lsts.ripples.model.soi;

import java.util.Date;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
@Index
public class SoiState {

	@Id
	public String name;
	
	public Date lastUpdated = null;
	
	public String asset;
}
