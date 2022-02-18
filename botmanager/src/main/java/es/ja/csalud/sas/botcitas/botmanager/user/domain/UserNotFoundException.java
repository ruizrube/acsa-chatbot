package es.ja.csalud.sas.botcitas.botmanager.user.domain;

public class UserNotFoundException extends Exception {

	public UserNotFoundException(String userIdentifier) {
		super("Could not find user " + userIdentifier);
	}

	
	
}
