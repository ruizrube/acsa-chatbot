package es.ja.csalud.sas.botcitas.botmanager.appoinment;

public class AppointmentNotFoundException extends RuntimeException {
	public AppointmentNotFoundException(String id) {
		super("Could not find appointment " + id);
	}
}
