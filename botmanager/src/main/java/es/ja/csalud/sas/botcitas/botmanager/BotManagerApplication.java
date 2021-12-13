package es.ja.csalud.sas.botcitas.botmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import es.ja.csalud.sas.botcitas.botmanager.appoinment.AppointmentService;
import es.ja.csalud.sas.botcitas.botmanager.user.User;
import es.ja.csalud.sas.botcitas.botmanager.user.UserService;

@SpringBootApplication
@EnableJpaRepositories
public class BotManagerApplication implements CommandLineRunner {

	@Autowired
	private UserService userService;

	@Autowired
	private AppointmentService appointmentService;

//	@Bean
//	public Docket api() {
//		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
//				.paths(PathSelectors.any()).build();
//	}

	public static void main(String[] args) {
		SpringApplication.run(BotManagerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		if (userService.count() == 0) {
			User doctor = new User();
			doctor.setFirstName("Doctor");
			doctor.setLastName("Facultativo");
			doctor.setIdentityDocument("0101010");
			doctor = userService.save(doctor);

			User user = new User();
			user.setFirstName("Pepe");
			user.setLastName("Andaluz");
			user.setIdentityDocument("111");
			user.setDoctor(doctor);
			user = userService.save(user);

			user = new User();
			user.setFirstName("María");
			user.setLastName("Andaluza");
			user.setIdentityDocument("222");
			user.setDoctor(doctor);
			user = userService.save(user);
			
			user = new User();
			user.setFirstName("Luis");
			user.setLastName("Andaluz");
			user.setIdentityDocument("333");
			user.setDoctor(doctor);
			user = userService.save(user);
			
			user = new User();
			user.setFirstName("Carmen");
			user.setLastName("Andaluza");
			user.setIdentityDocument("444");
			user.setDoctor(doctor);
			user = userService.save(user);

		}

		// TODO Auto-generated method stub

	}

}
