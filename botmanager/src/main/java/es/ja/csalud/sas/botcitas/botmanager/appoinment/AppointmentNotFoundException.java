package es.ja.csalud.sas.botcitas.botmanager.appoinment;

public class AppointmentNotFoundException extends Exception {
	public AppointmentNotFoundException(String id) {
		super("Could not find appointment " + id);
	}
}
