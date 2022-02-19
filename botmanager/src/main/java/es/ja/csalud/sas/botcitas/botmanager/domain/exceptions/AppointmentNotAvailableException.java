package es.ja.csalud.sas.botcitas.botmanager.domain.exceptions;

import java.time.LocalDateTime;

import es.ja.csalud.sas.botcitas.botmanager.domain.model.User;

public class AppointmentNotAvailableException  extends Exception {
	

	public AppointmentNotAvailableException(User doctor, LocalDateTime dateTime) {
		super("The temporary slot is not available");
	}
}