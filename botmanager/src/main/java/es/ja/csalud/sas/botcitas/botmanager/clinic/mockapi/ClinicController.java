package es.ja.csalud.sas.botcitas.botmanager.clinic.mockapi;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import es.ja.csalud.sas.botcitas.botmanager.clinic.Clinic;
import es.ja.csalud.sas.botcitas.botmanager.clinic.ClinicService;

@RestController
class ClinicController {

	private final ClinicService service;

	ClinicController(ClinicService service) {
		this.service = service;
	}

	// Aggregate root
	// tag::get-aggregate-root[]
	@GetMapping("/Clinics")
	List<Clinic> all() {
		return service.findAll();
	}
	// end::get-aggregate-root[]

	// Single item
	@GetMapping("/Clinics/{id}")
	Optional<Clinic> one(@PathVariable String id) {

		return service.findById(id);
	}

}