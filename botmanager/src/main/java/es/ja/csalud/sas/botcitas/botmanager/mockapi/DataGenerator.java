package es.ja.csalud.sas.botcitas.botmanager.mockapi;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.ja.csalud.sas.botcitas.botmanager.domain.model.Clinic;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.User;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.ClinicService;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.UserService;
import es.ja.csalud.sas.botcitas.botmanager.mockapi.services.AppointmentServiceImpl;

@Component
public class DataGenerator {

	
	private UserService userService;
	private ClinicService clinicService;
	private AppointmentServiceImpl appointmentService;


	@Scheduled(cron = "0 0 8,16 * * ?", zone = "Europe/Paris")
	public void resetData() {
		System.out.println(">>>>Cleaning appointment database....");
		List<User> users = userService.findAll();
		
		for(User user:users) {
			user.setAcceptConditions(false);
			user.setEnabled(false);
			userService.save(user);
		}
		
		appointmentService.deleteAll();
	}
	
	
	@Bean
	public CommandLineRunner loadUserData(AppointmentServiceImpl appointmentService, UserService userService, ClinicService clinicService) {
		this.appointmentService = appointmentService;
		this.userService = userService;
		this.clinicService = clinicService;
		return args -> {

			if (clinicService.count() == 0) {
				Clinic clinic = new Clinic();
				clinic.setCity("Cádiz");
				clinic.setPhone("956956956");
				clinic.setName("Loreto Puntales");
				clinic.setAddress("Hidroavión Numancia, 0");
				clinic = clinicService.save(clinic);

				User doctor = new User();
				doctor.setFirstName("Doctor");
				doctor.setLastName("Facultativo");
				doctor.setIdentityDocument("99999999Z");
				doctor.setNuhsa("9999999999");
				doctor.setClinic(clinic);
				doctor = userService.save(doctor);

				User user = new User();
				user.setFirstName("Pepe");
				user.setLastName("Andaluz");
				user.setIdentityDocument("11111111A");
				user.setNuhsa("1111111111");
				user.setDoctor(doctor);
				user.setClinic(clinic);				
				user = userService.save(user);

				user = new User();
				user.setFirstName("María");
				user.setLastName("Andaluza");
				user.setIdentityDocument("22222222B");
				user.setNuhsa("2222222222");
				user.setDoctor(doctor);
				user.setClinic(clinic);								
				user = userService.save(user);

				user = new User();
				user.setFirstName("Luis");
				user.setLastName("Andaluz");
				user.setIdentityDocument("33333333C");
				user.setNuhsa("3333333333");
				user.setDoctor(doctor);
				user.setClinic(clinic);								
				user = userService.save(user);

				user = new User();
				user.setFirstName("Carmen");
				user.setLastName("Andaluza");
				user.setIdentityDocument("44444444D");
				user.setNuhsa("4444444444");
				user.setDoctor(doctor);
				user.setClinic(clinic);								
				user = userService.save(user);

			}
		};
	}
}
