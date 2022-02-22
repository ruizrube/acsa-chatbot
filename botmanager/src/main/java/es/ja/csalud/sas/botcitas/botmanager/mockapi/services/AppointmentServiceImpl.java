package es.ja.csalud.sas.botcitas.botmanager.mockapi.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.AppointmentNotAvailableException;
import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.AppointmentNotFoundException;
import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.UserNotAssignedToClinicException;
import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.UserNotAssignedToDoctorException;
import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.UserNotFoundException;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.Appointment;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.AppointmentStatus;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.AppointmentType;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.Clinic;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.User;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.AppointmentService;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.UserService;
import es.ja.csalud.sas.botcitas.botmanager.mockapi.repositories.AppointmentRepository;

@Service
public class AppointmentServiceImpl implements AppointmentService {

	private AppointmentRepository appointmentRepository;
	private UserService userService;

	public AppointmentServiceImpl(UserService userService, AppointmentRepository appointmentRepository) {
		this.userService = userService;
		this.appointmentRepository = appointmentRepository;
		
	}
	
	
	public void deleteAll() {
		appointmentRepository.deleteAll();
	}

	@Override
	public List<Appointment> findPastAppointments(String userIdentifier) throws UserNotFoundException {
		Optional<User> user = userService.findByIdentifier(userIdentifier);
		if (user.isPresent()) {
			return appointmentRepository.findByUserAndDateTimeLessThanAndStatusOrderByDateTimeAsc(user,
					LocalDateTime.now(), AppointmentStatus.CREATED);
		} else {
			throw new UserNotFoundException(userIdentifier);
		}

	}

	@Override
	public Optional<Appointment> findNextAppointment(String userIdentifier) throws UserNotFoundException {
		Optional<User> user = userService.findByIdentifier(userIdentifier);
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
			throw new UserNotFoundException(userIdentifier);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Appointment registerAppointment(String userIdentifier, LocalDateTime dateTime, AppointmentType type)
			throws UserNotFoundException, AppointmentNotAvailableException {

		// Cancelamos cita previa que tuviese el usuario
		cancelAppointment(userIdentifier);

		return confirmAppointment(userIdentifier, dateTime, type, "");

	}

	private Appointment confirmAppointment(String userIdentifier, LocalDateTime dateTime, AppointmentType type,
			String subject) throws UserNotFoundException, AppointmentNotAvailableException {

		Optional<User> user = userService.findByIdentifier(userIdentifier);
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
			throw new UserNotFoundException(userIdentifier);
		}

	}

	private boolean checkAvailability(User doctor, Clinic clinic, LocalDateTime dateTime, AppointmentType type) {

		if(dateTime.isBefore(LocalDateTime.now())) {
			return false;
		}
		
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

		if(date.isBefore(LocalDate.now())) {
			return false;
		}
		
		if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY) || date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
			return false;
		}

		return true;

	}

	@Override
	public boolean cancelAppointment(String userIdentifier) throws UserNotFoundException {

		Optional<User> user = userService.findByIdentifier(userIdentifier);
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
			throw new UserNotFoundException(userIdentifier);
		}

	}

	@Override
	public Appointment findById(String id) throws AppointmentNotFoundException {
		return appointmentRepository.findById(id).orElseThrow(() -> new AppointmentNotFoundException(id));

	}

	@Override
	public List<Appointment> findAll() {
		return appointmentRepository.findAll();
	}

	@Override
	public List<LocalDate> findAvailableDaySlots(String userIdentifier, AppointmentType appointmentType,
			LocalDate firstDate)
			throws UserNotFoundException, UserNotAssignedToDoctorException, UserNotAssignedToClinicException {

		// appointmentType is ignored
		Optional<User> user = userService.findByIdentifier(userIdentifier);
		if (user.isPresent()) {
			if (!user.get().getDoctor().isPresent()) {
				throw new UserNotAssignedToDoctorException(userIdentifier);
			}
			if (!user.get().getClinic().isPresent()) {
				throw new UserNotAssignedToClinicException(userIdentifier);
			}
			List<LocalDate> result = new ArrayList<LocalDate>();

			int days = 1;
			while (result.size() < 3) {
				LocalDate aux = firstDate.plusDays(days);
				if (checkAvailability(user.get().getDoctor().get(), user.get().getClinic().get(), aux,
						appointmentType)) {
					result.add(aux);
				}
				days += 1;

			}
			return result;
		} else {
			throw new UserNotFoundException(userIdentifier);
		}

	}

	@Override
	public List<LocalTime> findAvailableHourSlots(String userIdentifier, AppointmentType appointmentType,
			LocalDate date, LocalTime firstHour)
			throws UserNotAssignedToClinicException, UserNotAssignedToDoctorException, UserNotFoundException {

		// appointmentType is ignored
		Optional<User> user = userService.findByIdentifier(userIdentifier);
		if (user.isPresent()) {
			if (!user.get().getDoctor().isPresent()) {
				throw new UserNotAssignedToDoctorException(userIdentifier);
			}
			if (!user.get().getClinic().isPresent()) {
				throw new UserNotAssignedToClinicException(userIdentifier);
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
			throw new UserNotFoundException(userIdentifier);
		}

	}


	
}
