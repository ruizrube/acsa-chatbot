/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.appoinment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;

import es.ja.csalud.sas.botcitas.botmanager.AgentResponses;
import es.ja.csalud.sas.botcitas.botmanager.DialogFlowHandler;
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotAssignedToClinicException;
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotAssignedToDoctorException;
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotFoundException;

/**
 * @author ivanruizrube
 *
 */
@Component
public class AppointmentIntentHandler extends DialogFlowHandler {

	@Autowired
	private AppointmentService appointmentService;

	/**
	 * Webhook for querying the next appointment with the user's doctor
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.query")
	public ActionResponse queryAppointmentIntent(ActionRequest request) {

		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		try {
			Optional<Appointment> appointmentOpt = appointmentService.findNextAppointment(userIdentifier);

			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();

				builder.add(renderNextAppointment(appointment));

			} else {
				builder.add(AgentResponses.getString("Responses.APPOINTMENT_NO") //$NON-NLS-1$
						+ AgentResponses.getString("Responses.APPOINTMENT_REQUEST"));
			}
		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();
		return actionResponse;
	}

	/**
	 * Webhook for the followup intent when the user responds yes to request for a
	 * new appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.query - yes")
	public ActionResponse queryAppointmentFollowupIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// El usuario no tenia cita asignada y quiere pedir una...
		triggerCustomEvent(builder, EVENT_REQUEST_APPOINTMENT);

		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}

	/**
	 * Webhook for modifying an appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.modify")
	public ActionResponse modifyAppointmentIntent(ActionRequest request) {

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		ResponseBuilder builder = getResponseBuilder(request);

		Optional<Appointment> appointmentOpt;
		try {
			appointmentOpt = appointmentService.findNextAppointment(userIdentifier);

			if (appointmentOpt.isPresent()) {

				triggerCustomEvent(builder, EVENT_REQUEST_APPOINTMENT);

			} else {
				builder.add(AgentResponses.getString("Responses.APPOINTMENT_NO") //$NON-NLS-1$
				);
			}

		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();
		return actionResponse;

	}

	/**
	 * Webhook for requesting a new appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request")
	public ActionResponse requestAppointmentIntent(ActionRequest request) {

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		ResponseBuilder builder = getResponseBuilder(request);

		Optional<Appointment> appointmentOpt;
		try {
			appointmentOpt = appointmentService.findNextAppointment(userIdentifier);

			String preTextResponse = "";
			if (appointmentOpt.isPresent()) {
				preTextResponse = AgentResponses.getString("Responses.ALREADY_APPOINTMENT") //$NON-NLS-1$
						+ renderNextAppointment(appointmentOpt.get());

			}

			ActionContext creatingAppointmentContext = new ActionContext(CONTEXT_APPOINTMENT_CREATING, 10); // $NON-NLS-1$

			// Read appointment type from request
			AppointmentType appointmentType = (AppointmentType) readEnumParameter(
					request.getParameter("appointmentType"), AppointmentType.class);

			// Write appointment type into context
			putObjectParameter(creatingAppointmentContext, "appointmentType", appointmentType);

			// Write last date proposed into context
			putDateParameter(creatingAppointmentContext, "lastDateProposed", LocalDate.now());

			proposeDatesToUser(userIdentifier, builder, creatingAppointmentContext, preTextResponse);

		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();
		return actionResponse;

	}

	/**
	 * Webhook for the followup intent when the user rejected the days proposed
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.otherday")
	public ActionResponse rejectDaysProposedForRequestAppointmentFollowupIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING);

		proposeDatesToUser(userIdentifier, builder, creatingAppointmentContext, "");

		ActionResponse actionResponse = builder.build();
		return actionResponse;
	}

	private void proposeDatesToUser(String userIdentifier, ResponseBuilder builder,
			ActionContext creatingAppointmentContext, String preTextResponse) {

		// Read lastDateProposed from context
		LocalDate lastDateProposed = readDateParameter(
				creatingAppointmentContext.getParameters().get("lastDateProposed")); //$NON-NLS-1$

		// Read appointment type from context
		AppointmentType appointmentType = (AppointmentType) readEnumParameter(
				creatingAppointmentContext.getParameters().get("appointmentType"), AppointmentType.class);

		try {
			// Obtain availability

			List<LocalDate> availableDays = appointmentService.findAvailableDaySlots(userIdentifier, appointmentType,
					lastDateProposed);

			if (availableDays.size() > 0) {

				// Write lastDateProposed into context
				putDateParameter(creatingAppointmentContext, "lastDateProposed",
						availableDays.get(availableDays.size() - 1));

				builder.add(creatingAppointmentContext);

				builder.add(preTextResponse + AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_1") //$NON-NLS-1$
						+ renderDates(availableDays) + AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_2"));
			} else {
				builder.add(preTextResponse + AgentResponses.getString("Responses.AVAILABLE_DAY_NO"));
				builder.removeContext(CONTEXT_APPOINTMENT_CREATING);
			}
		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		} catch (UserNotAssignedToDoctorException e) {
			builder.add(AgentResponses.getString("Responses.USER_NO_ASSIGNED_TO_DOCTOR")); //$NON-NLS-1$
		} catch (UserNotAssignedToClinicException e) {
			builder.add(AgentResponses.getString("Responses.USER_NO_ASSIGNED_TO_CLINIC")); //$NON-NLS-1$
		}
	}

	/**
	 * Webhook for the followup intent when the user has to select the day
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.dayselection")
	public ActionResponse selectDayForRequestAppointmentFollowupIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		// Read date from request
		LocalDate date = readDateParameter(request.getParameter("date")); //$NON-NLS-1$

		// Write date into context
		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING); // $NON-NLS-1$
		putDateParameter(creatingAppointmentContext, "date", date);

		// Write last time proposed into context
		putTimeParameter(creatingAppointmentContext, "lastTimeProposed", LocalTime.of(8, 0));

		proposeHoursToUser(userIdentifier, builder, creatingAppointmentContext);
		ActionResponse actionResponse = builder.build();
		return actionResponse;

	}

	/**
	 * Webhook for the followup intent when the user rejected the days proposed
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.dayselection.otherhour")
	public ActionResponse rejectHoursProposedForRequestAppointmentFollowupIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING); // $NON-NLS-1$

		proposeHoursToUser(userIdentifier, builder, creatingAppointmentContext);

		ActionResponse actionResponse = builder.build();
		return actionResponse;
	}

	private void proposeHoursToUser(String identityNumber, ResponseBuilder builder,
			ActionContext creatingAppointmentContext) {
		// Read date from request
		LocalDate date = readDateParameter(creatingAppointmentContext.getParameters().get("date")); //$NON-NLS-1$

		// Read lastTimeProposed from context
		LocalTime lastTimeProposed = readTimeParameter(
				creatingAppointmentContext.getParameters().get("lastTimeProposed")); //$NON-NLS-1$

		// Read appointment type from context
		AppointmentType appointmentType = (AppointmentType) readEnumParameter(
				creatingAppointmentContext.getParameters().get("appointmentType"), AppointmentType.class);

		if (date != null) {

			List<LocalTime> availableHours;
			try {

				availableHours = appointmentService.findAvailableHourSlots(identityNumber, appointmentType, date,
						lastTimeProposed);

				if (availableHours.size() > 0) {
					// Write lastTimeProposed into context
					putTimeParameter(creatingAppointmentContext, "lastTimeProposed",
							availableHours.get(availableHours.size() - 1));

					builder.add(creatingAppointmentContext);

					builder.add(
							AgentResponses.getString("Responses.AVAILABLE_HOURS_SLOTS_1") + renderHours(availableHours) //$NON-NLS-1$
									+ AgentResponses.getString("Responses.AVAILABLE_HOURS_SLOTS_2"));
				} else {
					builder.add(AgentResponses.getString("Responses.AVAILABLE_HOURS_NO"));
					builder.removeContext(CONTEXT_APPOINTMENT_CREATING);
				}

			} catch (UserNotFoundException e) {
				builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
			} catch (UserNotAssignedToDoctorException e) {
				builder.add(AgentResponses.getString("Responses.USER_NO_ASSIGNED_TO_DOCTOR")); //$NON-NLS-1$
			} catch (UserNotAssignedToClinicException e) {
				builder.add(AgentResponses.getString("Responses.USER_NO_ASSIGNED_TO_CLINIC")); //$NON-NLS-1$
			}

		}
	}

	/**
	 * Webhook for the followup intent when the user has to select the hour
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.dayselection.hourselection")
	public ActionResponse selectHourForRequestAppointmentFollowupIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read date from context
		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING); // $NON-NLS-1$
		LocalDate date = readDateParameter(creatingAppointmentContext.getParameters().get("date")); //$NON-NLS-1$

		// Read time from request
		LocalTime time = readTimeParameter(request.getParameter("time")); //$NON-NLS-1$

		// Write time into context
		putTimeParameter(creatingAppointmentContext, "time", time);

		LocalDateTime slotProposed = LocalDateTime.of(date, time);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		Optional<Appointment> appointmentOpt;
		try {
			appointmentOpt = appointmentService.findNextAppointment(userIdentifier);

			String preTextResponse = "";
			if (appointmentOpt.isPresent()) {
				preTextResponse = AgentResponses.getString("Responses.ALREADY_APPOINTMENT_1") //$NON-NLS-1$
						+ renderNextAppointment(appointmentOpt.get())
						+ AgentResponses.getString("Responses.ALREADY_APPOINTMENT_2") + " ";
			}

			builder.add(preTextResponse + AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION_1")
					+ renderDateTime(slotProposed) + AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION_2"));

		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();
		return actionResponse;

	}

	/**
	 * Webhook for the followup intent when the user has to select the hour
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.dayselection.hourselection.yes")
	public ActionResponse confirmForRequestAppointmentFollowupIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		// Read date and time from context
		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING); // $NON-NLS-1$
		LocalDate date = readDateParameter(creatingAppointmentContext.getParameters().get("date")); //$NON-NLS-1$
		LocalTime time = readTimeParameter(creatingAppointmentContext.getParameters().get("time")); //$NON-NLS-1$
		LocalDateTime slotProposed = LocalDateTime.of(date, time);

		// Read appointment type from context
		AppointmentType appointmentType = (AppointmentType) readEnumParameter(
				creatingAppointmentContext.getParameters().get("appointmentType"), AppointmentType.class);

		try {

			
			Appointment appointment = appointmentService.registerAppointment(userIdentifier, slotProposed,
					appointmentType);

			builder.add(
					AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATED") + renderNextAppointment(appointment));

		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$

		} catch (AppointmentNotAvailableException e) {
			builder.add(AgentResponses.getString("Responses.SLOT_NOT_AVAILABLE")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();
		return actionResponse;

	}

	/**
	 * Webhook for canceling the user's next appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.cancel")
	public ActionResponse cancelAppointmentIntent(ActionRequest request) {

		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		try {
			Optional<Appointment> appointmentOpt = appointmentService.findNextAppointment(userIdentifier);

			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();

				builder.add(renderNextAppointment(appointment)
						+ AgentResponses.getString("Responses.APPOINTMENT_CANCELATION"));

			} else {
				builder.add(AgentResponses.getString("Responses.APPOINTMENT_NO")); //$NON-NLS-1$
			}
		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();
		return actionResponse;
	}

	/**
	 * Webhook for the followup intent when the user has decided to cancel the next
	 * appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.cancel - yes")
	public ActionResponse cancelAppointmentFollowupIntent(ActionRequest request) {

		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		try {
			if (appointmentService.cancelAppointment(userIdentifier)) {
				builder.add(AgentResponses.getString("Responses.APPOINTMENT_CANCELED"));
			} else {
				builder.add(AgentResponses.getString("Responses.APPOINTMENT_NO_CANCELED"));
			}

		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();
		return actionResponse;
	}

	private String renderNextAppointment(Appointment appointment) {

		String result = AgentResponses.getString("Responses.NEXT_APPOINTMENT")
				+ renderDateTime(appointment.getDateTime());

		if (appointment.getType().equals(AppointmentType.FACE_TO_FACE)) {
			result += AgentResponses.getString("Responses.NEXT_APPOINTMENT_FACE_TO_FACE")
					+ appointment.getClinic().getName();
		} else {
			result += AgentResponses.getString("Responses.NEXT_APPOINTMENT_PHONE");

		}
		return result;

	}

}
