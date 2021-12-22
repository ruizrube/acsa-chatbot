package es.ja.csalud.sas.botcitas.botmanager.appoinment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import es.ja.csalud.sas.botcitas.botmanager.user.User;
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotFoundException;
import es.ja.csalud.sas.botcitas.botmanager.user.UserRepository;

@Service
public class AppointmentService {

	private AppointmentRepository appointmentRepository;
	private UserRepository userRepository;

	public AppointmentService(UserRepository userRepository, AppointmentRepository appointmentRepository) {
		this.userRepository = userRepository;
		this.appointmentRepository = appointmentRepository;
	}

	public List<Appointment> findPastAppointments(String userIdentityDocument) throws UserNotFoundException {
		Optional<User> user = userRepository.findByIdentityDocument(userIdentityDocument);
		if (user.isPresent()) {
			return appointmentRepository.findByUserAndDateTimeLessThan(user, LocalDateTime.now());
		} else {
			throw new UserNotFoundException(userIdentityDocument);
		}

	}

	public Optional<Appointment> findNextAppointment(String userIdentityDocument) throws UserNotFoundException {
		Optional<User> user = userRepository.findByIdentityDocument(userIdentityDocument);
		if (user.isPresent()) {

			List<Appointment> data = appointmentRepository.findByUserAndDateTimeGreaterThanEqualOrderByDateTimeAsc(user,
					LocalDateTime.now());
			if (data.size() > 0) {
				return Optional.of(data.get(0));
			} else {
				return Optional.empty();
			}

		} else {
			throw new UserNotFoundException(userIdentityDocument);
		}
	}

	
	
	
	public Appointment confirmAppointment(String userId, LocalDateTime dateTime, AppointmentType type, String subject)
			throws UserNotFoundException {

		Optional<User> user = userRepository.findByIdentityDocument(userId);
		if (user.isPresent()) {

			List<Appointment> appointments = appointmentRepository.findByAssignedDoctorAndDateTimeBetween(
					user.get().getDoctor().get(), dateTime.truncatedTo(ChronoUnit.HOURS),
					dateTime.plusHours(1).truncatedTo(ChronoUnit.HOURS));

			if (appointments.size() == 0) {
				Appointment appointment = new Appointment();
				appointment.setUser(user.get());
				appointment.setDateTime(dateTime);
				appointment.setType(type);
				appointment.setSubject(subject);
				appointment.setAssignedDoctor(user.get().getDoctor().get());
				appointment.setClinic(user.get().getClinic().get());
				return appointmentRepository.save(appointment);
			} else {
				throw new AppointmentNotAvailableException(user.get().getDoctor().get(), dateTime);
			}

		} else {
			throw new UserNotFoundException(userId);
		}

	}

	
	public Appointment findById(String id) {
		return appointmentRepository.findById(id).orElseThrow(() -> new AppointmentNotFoundException(id));

	}

	public List<Appointment> findAll() {
		return appointmentRepository.findAll();
	}

	
	
	
	public Appointment confirmAppointment(String userId, LocalDateTime dateTime, AppointmentType type)
			throws UserNotFoundException {

		return confirmAppointment(userId, dateTime, type, "");

	}

	public LocalDateTime findNextAvailableSlot(AppointmentType type) {

		return findNextAvailableSlotAfterDate(type, LocalDateTime.now());

	}

	public LocalDateTime findNextAvailableSlotAfterDate(AppointmentType type, LocalDateTime dateTime) {

		Random ran = new Random(System.currentTimeMillis());
		LocalDate newDate = dateTime.plusDays(ran.nextInt(10) + 1).toLocalDate();

		ran = new Random(System.currentTimeMillis());
		LocalTime newTime = LocalTime.of(ran.nextInt(12) + 8, 0);

		return LocalDateTime.of(newDate, newTime);

	}

	public List<LocalDate> findAvailableDaySlots(String identityNumber) {
		
		List<LocalDate> result= new ArrayList<LocalDate>();
		result.add(LocalDate.now().plusDays(1));
		result.add(LocalDate.now().plusDays(2));
		result.add(LocalDate.now().plusDays(3));
		return result;
	}

	public List<LocalTime> findAvailableHourSlots(LocalDate date, String identityNumber) {
		List<LocalTime> result= new ArrayList<LocalTime>();
		result.add(LocalTime.now().plusMinutes(15));
		result.add(LocalTime.now().plusMinutes(30));
		result.add(LocalTime.now().plusMinutes(45));
		return result;
	}

	
}
