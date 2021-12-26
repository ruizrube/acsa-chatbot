package es.ja.csalud.sas.botcitas.botmanager.appoinment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ja.csalud.sas.botcitas.botmanager.user.User;

public interface AppointmentRepository extends JpaRepository<Appointment, String>{

	List<Appointment> findByUserAndDateTimeLessThan(Optional<User> user, LocalDateTime dateTime);

	
	List<Appointment> findByAssignedDoctorAndDateTimeBetween(User doctor, LocalDateTime minusMinutes, LocalDateTime plusMinutes);

	List<Appointment> findByUserAndDateTimeGreaterThanEqualAndStatusOrderByDateTimeAsc(Optional<User> user,
			LocalDateTime now, AppointmentStatus status);

}
