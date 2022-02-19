package es.ja.csalud.sas.botcitas.botmanager.domain.services;

import java.util.List;
import java.util.Optional;

import es.ja.csalud.sas.botcitas.botmanager.domain.model.User;

public interface UserService {

	/**
	 * Save a given user
	 * @param user
	 * @return
	 */
	User save(User user);

	/**
	 * Get the total of users
	 * @return
	 */
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