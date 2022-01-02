package es.ja.csalud.sas.botcitas.botmanager.user;

public class UserNotAssignedToDoctorException extends Exception {
	public UserNotAssignedToDoctorException(String userIdentifier) {
		super("The user " + userIdentifier + " is not assigned to a doctor");
	}
}
