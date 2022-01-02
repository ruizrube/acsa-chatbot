package es.ja.csalud.sas.botcitas.botmanager.user;

import java.util.List;
import java.util.Optional;

public interface UserService {

	User save(User user);

	long count();

	
	/**
	 * Get a user by his/her identity national card or his/her NUHSA (Numero Unico Historia Salud Andalucia)
	 * @param identifier
	 * @return
	 */
	Optional<User> findByIdentifier(String identifier);
	


	/**
	 * Get all users
	 * @return
	 */
	List<User> findAll();

}