package es.ja.csalud.sas.botcitas.botmanager.appoinment.mockapi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ja.csalud.sas.botcitas.botmanager.appoinment.domain.Appointment;
import es.ja.csalud.sas.botcitas.botmanager.appoinment.domain.AppointmentStatus;
import es.ja.csalud.sas.botcitas.botmanager.user.domain.User;

public interface AppointmentRepository extends JpaRepository<Appointment, String>{

	
	
	long countByAssignedDoctorAndDateTimeBetween(User doctor, LocalDateTime minusMinutes, LocalDateTime plusMinutes);

	List<Appointment> findByUserAndDateTimeGreaterThanEqualAndStatusOrderByDateTimeAsc(Optional<User> user,
			LocalDateTime now, AppointmentStatus status);

	List<Appointment> findByUserAndDateTimeLessThanAndStatusOrderByDateTimeAsc(Optional<User> user, LocalDateTime now,
			AppointmentStatus status);

}
