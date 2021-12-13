package es.ja.csalud.sas.botcitas.botmanager.appoinment;

public enum AppointmentType {

	PHONE("Phone"), VIDEO("Video"), FACE_TO_FACE("Face_To_Face");

	public final String label;

	private AppointmentType(String label) {
		this.label = label;
	}

}
