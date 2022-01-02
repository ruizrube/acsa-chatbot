package es.ja.csalud.sas.botcitas.botmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import es.ja.csalud.sas.botcitas.botmanager.appoinment.AppointmentService;
import es.ja.csalud.sas.botcitas.botmanager.clinic.Clinic;
import es.ja.csalud.sas.botcitas.botmanager.clinic.ClinicService;
import es.ja.csalud.sas.botcitas.botmanager.user.User;
import es.ja.csalud.sas.botcitas.botmanager.user.UserService;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement

public class BotManagerApplication implements CommandLineRunner {

	@Autowired
	private UserService userService;

	@Autowired
	private ClinicService clinicService;

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

		if (clinicService.count() == 0) {
			Clinic clinic= new Clinic();
			clinic.setCity("Cádiz");
			clinic.setPhone("956956956");
			clinic.setAddress("Hidroavión Numancia, 0");
			clinic = clinicService.save(clinic);
		}
		
		if (userService.count() == 0) {
			User doctor = new User();
			doctor.setFirstName("Doctor");
			doctor.setLastName("Facultativo");
			doctor.setIdentityDocument("0101010");
			doctor.setNuhsa("0000000000");
			doctor = userService.save(doctor);

			User user = new User();
			user.setFirstName("Pepe");
			user.setLastName("Andaluz");
			user.setIdentityDocument("111");
			user.setNuhsa("1111111111");			
			user.setDoctor(doctor);
			user = userService.save(user);

			user = new User();
			user.setFirstName("María");
			user.setLastName("Andaluza");
			user.setIdentityDocument("222");
			user.setNuhsa("2222222222");			
			user.setDoctor(doctor);
			user = userService.save(user);
			
			user = new User();
			user.setFirstName("Luis");
			user.setLastName("Andaluz");
			user.setIdentityDocument("333");
			user.setNuhsa("3333333333");			
			user.setDoctor(doctor);
			user = userService.save(user);
			
			user = new User();
			user.setFirstName("Carmen");
			user.setLastName("Andaluza");
			user.setIdentityDocument("444");
			user.setNuhsa("4444444444");						
			user.setDoctor(doctor);
			user = userService.save(user);

		}

		// TODO Auto-generated method stub

	}

}
