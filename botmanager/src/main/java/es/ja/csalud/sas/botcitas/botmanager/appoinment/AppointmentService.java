package es.ja.csalud.sas.botcitas.botmanager.appoinment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import es.ja.csalud.sas.botcitas.botmanager.user.UserNotAssignedToClinicException;
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotAssignedToDoctorException;
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotFoundException;

public interface AppointmentService {

	/**
	 * Obtain the user's past appointments
	 * 
	 * @param userIdentifier
	 * @return
	 * @throws UserNotFoundException
	 */
	List<Appointment> findPastAppointments(String userIdentifier) throws UserNotFoundException;

	/**
	 * Obtain the user's next appointment
	 * 
	 * @param userIdentifier
	 * @return
	 * @throws UserNotFoundException
	 */
	Optional<Appointment> findNextAppointment(String userIdentifier) throws UserNotFoundException;

	/**
	 * Register a new appointment for the user, canceling any previous appointment
	 * 
	 * @param userIdentifier
	 * @param dateTime
	 * @param type
	 * @return
	 * @throws UserNotFoundException
	 * @throws AppointmentNotAvailableException
	 */
	Appointment registerAppointment(String userIdentifier, LocalDateTime dateTime, AppointmentType type)
			throws UserNotFoundException, AppointmentNotAvailableException;

	/**
	 * Cancel the user's next appointment
	 * 
	 * @param userIdentifier
	 * @return
	 * @throws UserNotFoundException
	 */
	boolean cancelAppointment(String userIdentifier) throws UserNotFoundException;

	/**
	 * Obtain a given appointment
	 * 
	 * @param id
	 * @return
	 */
	Appointment findById(String id) throws AppointmentNotFoundException;

	/**
	 * Obtain all the appointments stored
	 * 
	 * @return
	 */
	List<Appointment> findAll();


	/**
	 * Obtain available dates for a new appointment
	 * @param userIdentifier
	 * @param appointmentType
	 * @param firstDate
	 * @return
	 * @throws UserNotFoundException
	 * @throws UserNotAssignedToDoctorException
	 * @throws UserNotAssignedToClinicException
	 */
	List<LocalDate> findAvailableDaySlots(String userIdentifier, AppointmentType appointmentType,
			LocalDate firstDate)
			throws UserNotFoundException, UserNotAssignedToDoctorException, UserNotAssignedToClinicException;

	/**
	 * Obtain available times for a new appointment in a given date
	 * @param userIdentifier
	 * @param appointmentType
	 * @param date
	 * @param firstHour
	 * @return
	 * @throws UserNotAssignedToClinicException
	 * @throws UserNotAssignedToDoctorException
	 * @throws UserNotFoundException
	 */
	List<LocalTime> findAvailableHourSlots(String userIdentifier, AppointmentType appointmentType, LocalDate date,
			LocalTime firstHour)
			throws UserNotAssignedToClinicException, UserNotAssignedToDoctorException, UserNotFoundException;

}