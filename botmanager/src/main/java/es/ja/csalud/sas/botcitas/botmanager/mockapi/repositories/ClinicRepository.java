package es.ja.csalud.sas.botcitas.botmanager.mockapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ja.csalud.sas.botcitas.botmanager.domain.model.Clinic;

public interface ClinicRepository extends JpaRepository<Clinic, String>{

	
}
