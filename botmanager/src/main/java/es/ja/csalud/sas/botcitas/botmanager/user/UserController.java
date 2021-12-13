package es.ja.csalud.sas.botcitas.botmanager.user;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserController {

	private final UserService service;

	UserController(UserService service) {
		this.service = service;
	}

	// Aggregate root
	// tag::get-aggregate-root[]
	@GetMapping("/Users")
	List<User> all() {
		return service.findAll();
	}
	// end::get-aggregate-root[]

	// Single item
	@GetMapping("/Users/{id}")
	Optional<User> one(@PathVariable String id) {

		return service.findById(id);
	}

}