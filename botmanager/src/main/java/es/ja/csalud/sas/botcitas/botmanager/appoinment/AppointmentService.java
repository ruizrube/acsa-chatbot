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
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotAssignedToClinicException;
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotAssignedToDoctorException;
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

	/**
	 * Obtain the user's past appointments
	 * 
	 * @param userIdentityDocument
	 * @return
	 * @throws UserNotFoundException
	 */
	public List<Appointment> findPastAppointments(String userIdentityDocument) throws UserNotFoundException {
		Optional<User> user = userRepository.findByIdentityDocument(userIdentityDocument);
		if (user.isPresent()) {
			return appointmentRepository.findByUserAndDateTimeLessThanAndStatusOrderByDateTimeAsc(user,
					LocalDateTime.now(), AppointmentStatus.CREATED);
		} else {
			throw new UserNotFoundException(userIdentityDocument);
		}

	}

	/**
	 * Obtain the user's next appointment
	 * 
	 * @param userIdentityDocument
	 * @return
	 * @throws UserNotFoundException
	 */
	public Optional<Appointment> findNextAppointment(String userIdentityDocument) throws UserNotFoundException {
		Optional<User> user = userRepository.findByIdentityDocument(userIdentityDocument);
		if (user.isPresent()) {

			List<Appointment> data = appointmentRepository
					.findByUserAndDateTimeGreaterThanEqualAndStatusOrderByDateTimeAsc(user, LocalDateTime.now(),
							AppointmentStatus.CREATED);
			if (data.size() > 0) {
				return Optional.of(data.get(0));
			} else {
				return Optional.empty();
			}

		} else {
			throw new UserNotFoundException(userIdentityDocument);
		}
	}

	/**
	 * Register a new appointment for the user
	 * 
	 * @param userId
	 * @param dateTime
	 * @param type
	 * @return
	 * @throws UserNotFoundException
	 */
	public Appointment confirmAppointment(String userId, LocalDateTime dateTime, AppointmentType type)
			throws UserNotFoundException {

		return confirmAppointment(userId, dateTime, type, "");

	}

	private Appointment confirmAppointment(String userIdentityDocument, LocalDateTime dateTime, AppointmentType type,
			String subject) throws UserNotFoundException {

		Optional<User> user = userRepository.findByIdentityDocument(userIdentityDocument);
		if (user.isPresent()) {

			List<Appointment> appointments = appointmentRepository.findByAssignedDoctorAndDateTimeBetween(
					user.get().getDoctor().get(), dateTime.truncatedTo(ChronoUnit.HOURS),
					dateTime.plusHours(1).truncatedTo(ChronoUnit.HOURS));

			if (appointments.size() == 0) {
				Appointment appointment = new Appointment();
				appointment.setUser(user.get());
				appointment.setDateTime(dateTime);
				appointment.setType(type);
				appointment.setStatus(AppointmentStatus.CREATED);
				appointment.setSubject(subject);
				appointment.setAssignedDoctor(user.get().getDoctor().get());
				appointment.setClinic(user.get().getClinic().get());
				return appointmentRepository.save(appointment);
			} else {
				throw new AppointmentNotAvailableException(user.get().getDoctor().get(), dateTime);
			}

		} else {
			throw new UserNotFoundException(userIdentityDocument);
		}

	}

	/**
	 * Cancel the user's next appointment
	 * 
	 * @param userIdentityDocument
	 * @return
	 * @throws UserNotFoundException
	 */
	public boolean cancelAppointment(String userIdentityDocument) throws UserNotFoundException {

		Optional<User> user = userRepository.findByIdentityDocument(userIdentityDocument);
		if (user.isPresent()) {

			List<Appointment> data = appointmentRepository
					.findByUserAndDateTimeGreaterThanEqualAndStatusOrderByDateTimeAsc(user, LocalDateTime.now(),
							AppointmentStatus.CREATED);
			if (data.size() > 0) {
				Appointment theAppointment = data.get(data.size() - 1);
				theAppointment.setStatus(AppointmentStatus.CANCELED);
				appointmentRepository.save(theAppointment);
				return true;
			} else {
				return false;
			}
		} else {
			throw new UserNotFoundException(userIdentityDocument);
		}

	}

	/**
	 * Obtain a given appointment
	 * 
	 * @param id
	 * @return
	 */
	public Appointment findById(String id) {
		return appointmentRepository.findById(id).orElseThrow(() -> new AppointmentNotFoundException(id));

	}

	/**
	 * Obtain all the appointments stored
	 * 
	 * @return
	 */
	public List<Appointment> findAll() {
		return appointmentRepository.findAll();
	}

	public LocalDateTime findNextAvailableSlotAfterDate(AppointmentType type, LocalDateTime dateTime) {

		Random ran = new Random(System.currentTimeMillis());
		LocalDate newDate = dateTime.plusDays(ran.nextInt(10) + 1).toLocalDate();

		ran = new Random(System.currentTimeMillis());
		LocalTime newTime = LocalTime.of(ran.nextInt(12) + 8, 0);

		return LocalDateTime.of(newDate, newTime);

	}

	public List<LocalDate> findAvailableDaySlots(String userIdentityDocument, AppointmentType appointmentType,
			LocalDate firstDate)
			throws UserNotFoundException, UserNotAssignedToDoctorException, UserNotAssignedToClinicException {

		// appointmentType is ignored
		Optional<User> user = userRepository.findByIdentityDocument(userIdentityDocument);
		if (user.isPresent()) {
			if (!user.get().getDoctor().isPresent()) {
				throw new UserNotAssignedToDoctorException(userIdentityDocument);
			}
			if (!user.get().getClinic().isPresent()) {
				throw new UserNotAssignedToClinicException(userIdentityDocument);
			}
			List<LocalDate> result = new ArrayList<LocalDate>();
			result.add(firstDate.plusDays(1));
			result.add(firstDate.plusDays(2));
			result.add(firstDate.plusDays(3));
			return result;
		} else {
			throw new UserNotFoundException(userIdentityDocument);
		}

	}

	public List<LocalTime> findAvailableHourSlots(String userIdentityDocument, AppointmentType appointmentType,
			LocalDate date, LocalTime firstHour)
			throws UserNotAssignedToClinicException, UserNotAssignedToDoctorException, UserNotFoundException {

		// appointmentType is ignored
		Optional<User> user = userRepository.findByIdentityDocument(userIdentityDocument);
		if (user.isPresent()) {
			if (!user.get().getDoctor().isPresent()) {
				throw new UserNotAssignedToDoctorException(userIdentityDocument);
			}
			if (!user.get().getClinic().isPresent()) {
				throw new UserNotAssignedToClinicException(userIdentityDocument);
			}
			List<LocalTime> result = new ArrayList<LocalTime>();

			result.add(firstHour.plusMinutes(15));
			result.add(firstHour.plusMinutes(30));
			result.add(firstHour.plusMinutes(45));
			return result;
		} else {
			throw new UserNotFoundException(userIdentityDocument);
		}

	}

}
