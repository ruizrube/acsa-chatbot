package es.ja.csalud.sas.botcitas.botmanager.user;

import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.ja.csalud.sas.botcitas.botmanager.clinic.Clinic;

@Entity
public class User {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")

	private String id;
	private String firstName;
	private String lastName;
	private String nuhsa;
	private String identityDocument;
	private boolean enabled;
	private boolean acceptConditions;

	@ManyToOne
	private Clinic clinic;

	@ManyToOne
	@JsonIgnore
	private User doctor;

	public String getId() {
		return id;
	}

	public String getNuhsa() {
		return nuhsa;
	}

	public void setNuhsa(String nuhsa) {
		this.nuhsa = nuhsa;
	}

	public String getIdentityDocument() {
		return identityDocument;
	}

	public void setIdentityDocument(String identityDocument) {
		this.identityDocument = identityDocument;
	}

	public String getName() {
		return firstName + " " + lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Optional<User> getDoctor() {
		return Optional.ofNullable(doctor);
	}

	public void setDoctor(User doctor) {
		this.doctor = doctor;
	}

	public Optional<Clinic> getClinic() {
		return Optional.ofNullable(clinic);
	}

	public void setClinic(Clinic clinic) {
		this.clinic = clinic;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isAcceptConditions() {
		return acceptConditions;
	}

	public void setAcceptConditions(boolean acceptConditions) {
		this.acceptConditions = acceptConditions;
	}

}
