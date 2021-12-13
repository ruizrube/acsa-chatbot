package es.ja.csalud.sas.botcitas.botmanager.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String>{

	Optional<User> findByIdentityDocument(String userIdentityDocument);

}
