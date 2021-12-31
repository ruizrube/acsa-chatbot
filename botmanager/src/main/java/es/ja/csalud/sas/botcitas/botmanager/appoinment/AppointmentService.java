package es.ja.csalud.sas.botcitas.botmanager.appoinment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import es.ja.csalud.sas.botcitas.botmanager.clinic.Clinic;
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
	 * Register a new appointment for the user, canceling any previous appointment
	 * 
	 * @param userId
	 * @param dateTime
	 * @param type
	 * @return
	 * @throws UserNotFoundException
	 * @throws AppointmentNotAvailableException
	 */
	@Transactional(rollbackFor = Exception.class)
	public Appointment registerAppointment(String userIdentityDocument, LocalDateTime dateTime, AppointmentType type)
			throws UserNotFoundException, AppointmentNotAvailableException {

		// Cancelamos cita previa que tuviese el usuario
		cancelAppointment(userIdentityDocument);

		return confirmAppointment(userIdentityDocument, dateTime, type, "");

	}

	private Appointment confirmAppointment(String userIdentityDocument, LocalDateTime dateTime, AppointmentType type,
			String subject) throws UserNotFoundException, AppointmentNotAvailableException {

		Optional<User> user = userRepository.findByIdentityDocument(userIdentityDocument);
		if (user.isPresent()) {

			if (checkAvailability(user.get().getDoctor().get(), user.get().getClinic().get(), dateTime, type)) {
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

	private boolean checkAvailability(User doctor, Clinic clinic, LocalDateTime dateTime, AppointmentType type) {

		if (dateTime.getDayOfWeek().equals(DayOfWeek.SATURDAY) || dateTime.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
			return false;
		}

		if (dateTime.getHour() < 8 || dateTime.getHour() > 15) {
			return false;
		}

		long countAppointments = appointmentRepository.countByAssignedDoctorAndDateTimeBetween(doctor,
				dateTime.truncatedTo(ChronoUnit.MINUTES), dateTime.plusMinutes(15).truncatedTo(ChronoUnit.MINUTES));

		if (countAppointments > 0) {
			return false;
		}

		return true;

	}

	private boolean checkAvailability(User doctor, Clinic clinic, LocalDate date, AppointmentType type) {

		if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY) || date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
			return false;
		}

		return true;

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

			int days = 1;
			while (result.size() < 3) {
				LocalDate aux = firstDate.plusDays(days);
				if (checkAvailability(user.get().getDoctor().get(), user.get().getClinic().get(), aux,
						appointmentType)) {
					result.add(aux);
				}
				days+=1;

			}
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

			int minutes = 15;
			while (result.size() < 3) {
				LocalDateTime aux = LocalDateTime.of(date, firstHour.plusMinutes(minutes));
				if (checkAvailability(user.get().getDoctor().get(), user.get().getClinic().get(), aux,
						appointmentType)) {
					result.add(aux.toLocalTime());
				}
				minutes += 15;
			}

			return result;
		} else {
			throw new UserNotFoundException(userIdentityDocument);
		}

	}

}
