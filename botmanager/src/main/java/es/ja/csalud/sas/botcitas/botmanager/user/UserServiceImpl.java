package es.ja.csalud.sas.botcitas.botmanager.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

	private UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public User save(User user) {
		return this.userRepository.saveAndFlush(user);
	}

	@Override
	public long count() {
		
		return userRepository.count();
	}

	
	@Override
	public Optional<User> findByIdentifier(String identifier) {
		identifier=identifier.replace("AN","").trim();
		return userRepository.findByIdentityDocumentOrNuhsa(identifier,identifier);
	}

	

	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}


	
}
