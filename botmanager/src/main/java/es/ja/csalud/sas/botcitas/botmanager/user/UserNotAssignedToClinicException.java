package es.ja.csalud.sas.botcitas.botmanager.user;

public class UserNotAssignedToClinicException extends Exception {
	public UserNotAssignedToClinicException(String identityDocument) {
		super("The user " + identityDocument + " is not assigned to a clinic");
	}
}
