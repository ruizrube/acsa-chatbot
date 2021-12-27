package es.ja.csalud.sas.botcitas.botmanager.user;

public class UserNotAssignedToDoctorException extends Exception {
	public UserNotAssignedToDoctorException(String identityDocument) {
		super("The user " + identityDocument + " is not assigned to a doctor");
	}
}
