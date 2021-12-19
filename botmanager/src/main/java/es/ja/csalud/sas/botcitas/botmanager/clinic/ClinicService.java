package es.ja.csalud.sas.botcitas.botmanager.clinic;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class ClinicService {

	private ClinicRepository repository;

	public ClinicService(ClinicRepository ClinicRepository) {
		this.repository = ClinicRepository;
	}

	public Clinic save(Clinic Clinic) {
		return this.repository.saveAndFlush(Clinic);
	}

	public long count() {
		
		return repository.count();
	}

	public Optional<Clinic> findById(String id) {
		return repository.findById(id);
	}

	public List<Clinic> findAll() {
		return repository.findAll();
	}


	
}
