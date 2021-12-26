package es.ja.csalud.sas.botcitas.botmanager.appoinment;

public enum AppointmentStatus {

	CREATED("creada"), CANCELED("cancelada");

	private final String label;

	private AppointmentStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
