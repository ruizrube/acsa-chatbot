package es.ja.csalud.sas.botcitas.botmanager.appoinment;

public enum AppointmentType {

	PHONE("telefónica"), FACE_TO_FACE("presencial");

	private final String label;

	private AppointmentType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
