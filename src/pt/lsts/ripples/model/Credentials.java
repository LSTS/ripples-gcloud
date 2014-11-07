package pt.lsts.ripples.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Cache
public class Credentials {
	@Id
	public String name;
	public String login;
	public String password;
}
