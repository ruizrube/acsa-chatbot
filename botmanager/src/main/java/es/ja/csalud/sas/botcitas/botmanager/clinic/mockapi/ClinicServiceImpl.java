package es.ja.csalud.sas.botcitas.botmanager.clinic.mockapi;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import es.ja.csalud.sas.botcitas.botmanager.clinic.Clinic;
import es.ja.csalud.sas.botcitas.botmanager.clinic.ClinicService;

@Service
public class ClinicServiceImpl implements ClinicService {

	private ClinicRepository repository;

	public ClinicServiceImpl(ClinicRepository ClinicRepository) {
		this.repository = ClinicRepository;
	}

	@Override
	public Clinic save(Clinic Clinic) {
		return this.repository.saveAndFlush(Clinic);
	}

	@Override
	public long count() {
		
		return repository.count();
	}

	@Override
	public Optional<Clinic> findById(String id) {
		return repository.findById(id);
	}

	@Override
	public List<Clinic> findAll() {
		return repository.findAll();
	}


	
}
