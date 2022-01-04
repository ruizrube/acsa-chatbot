package es.ja.csalud.sas.botcitas.botmanager.user.mockapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import es.ja.csalud.sas.botcitas.botmanager.clinic.Clinic;
import es.ja.csalud.sas.botcitas.botmanager.clinic.ClinicService;
import es.ja.csalud.sas.botcitas.botmanager.user.User;
import es.ja.csalud.sas.botcitas.botmanager.user.UserService;

@Component
public class UserDataGenerator {

	@Bean
	public CommandLineRunner loadUserData(UserService userService, ClinicService clinicService) {
		return args -> {

			if (clinicService.count() == 0) {
				Clinic clinic = new Clinic();
				clinic.setCity("Cádiz");
				clinic.setPhone("956956956");
				clinic.setAddress("Hidroavión Numancia, 0");
				clinic = clinicService.save(clinic);

				User doctor = new User();
				doctor.setFirstName("Doctor");
				doctor.setLastName("Facultativo");
				doctor.setIdentityDocument("0101010");
				doctor.setNuhsa("0000000000");
				doctor.setClinic(clinic);
				doctor = userService.save(doctor);

				User user = new User();
				user.setFirstName("Pepe");
				user.setLastName("Andaluz");
				user.setIdentityDocument("111");
				user.setNuhsa("1111111111");
				user.setDoctor(doctor);
				user.setClinic(clinic);				
				user = userService.save(user);

				user = new User();
				user.setFirstName("María");
				user.setLastName("Andaluza");
				user.setIdentityDocument("222");
				user.setNuhsa("2222222222");
				user.setDoctor(doctor);
				user.setClinic(clinic);								
				user = userService.save(user);

				user = new User();
				user.setFirstName("Luis");
				user.setLastName("Andaluz");
				user.setIdentityDocument("333");
				user.setNuhsa("3333333333");
				user.setDoctor(doctor);
				user.setClinic(clinic);								
				user = userService.save(user);

				user = new User();
				user.setFirstName("Carmen");
				user.setLastName("Andaluza");
				user.setIdentityDocument("444");
				user.setNuhsa("4444444444");
				user.setDoctor(doctor);
				user.setClinic(clinic);								
				user = userService.save(user);

			}
		};
	}
}
