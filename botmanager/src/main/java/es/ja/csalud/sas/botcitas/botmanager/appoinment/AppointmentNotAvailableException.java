package es.ja.csalud.sas.botcitas.botmanager.appoinment;

import java.time.LocalDateTime;

import es.ja.csalud.sas.botcitas.botmanager.user.User;

public class AppointmentNotAvailableException  extends RuntimeException {
	

	public AppointmentNotAvailableException(User doctor, LocalDateTime dateTime) {
		super("The temporary slot is not available");
	}
}