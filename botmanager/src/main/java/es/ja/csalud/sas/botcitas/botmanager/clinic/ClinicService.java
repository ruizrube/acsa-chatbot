package es.ja.csalud.sas.botcitas.botmanager.clinic;

import java.util.List;
import java.util.Optional;

public interface ClinicService {

	Clinic save(Clinic Clinic);

	long count();

	Optional<Clinic> findById(String id);

	List<Clinic> findAll();

}