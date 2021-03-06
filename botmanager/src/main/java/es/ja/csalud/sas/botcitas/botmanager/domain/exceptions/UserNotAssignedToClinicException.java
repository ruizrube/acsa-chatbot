package es.ja.csalud.sas.botcitas.botmanager.domain.exceptions;

public class UserNotAssignedToClinicException extends Exception {
	public UserNotAssignedToClinicException(String userIdentifier) {
		super("The user " + userIdentifier + " is not assigned to a clinic");
	}
}
