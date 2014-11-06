package pt.lsts.ripples.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Credentials {
	@Id
	String name;
	String login;
	String password;
}
