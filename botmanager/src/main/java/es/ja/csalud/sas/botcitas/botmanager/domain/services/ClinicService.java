package es.ja.csalud.sas.botcitas.botmanager.domain.services;

import java.util.List;
import java.util.Optional;

import es.ja.csalud.sas.botcitas.botmanager.domain.model.Clinic;

public interface ClinicService {

	Clinic save(Clinic Clinic);

	long count();

	Optional<Clinic> findById(String id);

	List<Clinic> findAll();

}