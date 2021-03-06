package es.ja.csalud.sas.botcitas.botmanager.mockapi.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ja.csalud.sas.botcitas.botmanager.domain.model.User;

public interface UserRepository extends JpaRepository<User, String>{

	Optional<User> findByIdentityDocument(String userIdentityDocument);

	Optional<User> findByNuhsa(String nuhsa);

	Optional<User> findByIdentityDocumentOrNuhsa(String userIdentityDocument, String nuhsa);

}
