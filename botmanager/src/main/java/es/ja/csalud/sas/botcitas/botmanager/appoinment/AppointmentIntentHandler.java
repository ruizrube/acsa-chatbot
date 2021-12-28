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
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$	
		
		try {
			Optional<Appointment> appointmentOpt = appointmentService.findNextAppointment(identityNumber);

			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();

				builder.add(renderNextAppointment(appointment));

			} else {
				builder.add(AgentResponses.getString("Responses.APPOINTMENT_NO") + AgentResponses.getString("Responses.APPOINTMENT_REQUEST")); //$NON-NLS-1$
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
	 * Webhook for requesting a new appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request")
	public ActionResponse requestAppointmentIntent(ActionRequest request) {

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		ResponseBuilder builder = getResponseBuilder(request);

		try {
			Optional<Appointment> appointmentOpt = appointmentService.findNextAppointment(identityNumber);

			if (!appointmentOpt.isPresent()) {

				List<LocalDate> availableDays = appointmentService.findAvailableDaySlots(identityNumber);
				builder.add(AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_1") + renderDates(availableDays) //$NON-NLS-1$
						+ AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_2"));

				ActionContext creatingAppointmentContext = new ActionContext(CONTEXT_APPOINTMENT_CREATING, 10); // $NON-NLS-1$

				// Read appointment type from request
				AppointmentType appointmentType = (AppointmentType) readEnumParameter(
						request.getParameter("appointmentType"), AppointmentType.class);

				// Write appointment type into context
				putParameter(creatingAppointmentContext, "appointmentType", appointmentType);

				builder.add(creatingAppointmentContext);

			} else {
				builder.add(AgentResponses.getString("Responses.ALREADY_APPOINTMENT") //$NON-NLS-1$
						+ renderNextAppointment(appointmentOpt.get()));
			}
		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		} catch (UserNotAssignedToDoctorException e) {
			builder.add(AgentResponses.getString("Responses.USER_NO_ASSIGNED_TO_DOCTOR")); //$NON-NLS-1$
		} catch (UserNotAssignedToClinicException e) {
			builder.add(AgentResponses.getString("Responses.USER_NO_ASSIGNED_TO_CLINIC")); //$NON-NLS-1$
		} 

		ActionResponse actionResponse = builder.build();
		return actionResponse;

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
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		// Read date from request
		LocalDate date = readDateParameter(request.getParameter("date")); //$NON-NLS-1$
		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING); // $NON-NLS-1$

		// Write date into context
		putParameter(creatingAppointmentContext, "date", date);

		List<LocalTime> availableHours = appointmentService.findAvailableHourSlots(date, identityNumber);

		builder.add(AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_1") + renderHours(availableHours) //$NON-NLS-1$
				+ AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_2"));

		ActionResponse actionResponse = builder.build();
		return actionResponse;

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
		putParameter(creatingAppointmentContext, "time", time);

		LocalDateTime slotProposed = LocalDateTime.of(date, time);

		builder.add(AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION_1") + renderDateTime(slotProposed)
				+ AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION_2"));

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
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		// Read date and time from context
		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING); // $NON-NLS-1$
		LocalDate date = readDateParameter(creatingAppointmentContext.getParameters().get("date")); //$NON-NLS-1$
		LocalTime time = readTimeParameter(creatingAppointmentContext.getParameters().get("time")); //$NON-NLS-1$
		LocalDateTime slotProposed = LocalDateTime.of(date, time);

		// Read appointment type from context
		AppointmentType appointmentType = (AppointmentType) readEnumParameter(
				creatingAppointmentContext.getParameters().get("appointmentType"), AppointmentType.class);

		try {
			Appointment appointment = appointmentService.confirmAppointment(identityNumber, slotProposed,
					appointmentType);

			builder.add(
					AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATED") + renderNextAppointment(appointment));

		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$

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
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$	
		
		try {
			Optional<Appointment> appointmentOpt = appointmentService.findNextAppointment(identityNumber);

			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();

				builder.add(renderNextAppointment(appointment) + AgentResponses.getString("Responses.APPOINTMENT_CANCELATION"));

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
	 * Webhook for the followup intent when the user has decided to cancel the next appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.cancel - yes")
	public ActionResponse cancelAppointmentFollowupIntent(ActionRequest request) {

		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$	
		
		try {
			if(appointmentService.cancelAppointment(identityNumber)) {
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
