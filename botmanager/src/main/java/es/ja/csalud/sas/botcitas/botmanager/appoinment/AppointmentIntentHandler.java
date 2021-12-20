/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.appoinment;

import java.time.LocalDateTime;
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

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		ResponseBuilder builder;

		try {
			Optional<Appointment> appointmentOpt = appointmentService.findNextAppointment(identityNumber);

			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();
				builder = getResponseBuilder(request);
				builder.add(AgentResponses.getString("Responses.NEXT_APPOINTMENT_1") //$NON-NLS-1$
						+ renderDateTime(appointment.getDateTime())
						+ AgentResponses.getString("Responses.NEXT_APPOINTMENT_2") + appointment.getClinic().getName());

			} else {

				builder = getResponseBuilder(request);
				builder.add(AgentResponses.getString("Responses.NO_APPOINTMENT")); //$NON-NLS-1$
			}
		} catch (UserNotFoundException e) {
			builder = getResponseBuilder(request);
			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
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

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		ResponseBuilder builder = getResponseBuilder(request);

		try {
			Optional<Appointment> appointmentOpt = appointmentService.findNextAppointment(identityNumber);

			if (!appointmentOpt.isPresent()) {
				// List<LocalTime> availableDays =
				// retrieveDays(appointmentService.findAvailableSlots());
				builder.add(AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_1") + renderDates() //$NON-NLS-1$
						+ AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_2"));

			} else {

				builder = getResponseBuilder(request);
				builder.add(AgentResponses.getString("Responses.ALREADY_APPOINTMENT")); //$NON-NLS-1$
			}
		} catch (UserNotFoundException e) {
			builder = getResponseBuilder(request);
			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
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

		builder.add(AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_1") + renderHours() //$NON-NLS-1$
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

		builder.add(AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION_1") + renderDateTime(LocalDateTime.now()) + AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION_2"));

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

		builder.add(AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATED") + renderDateTime(LocalDateTime.now()) );

		ActionResponse actionResponse = builder.build();
		return actionResponse;

	}
	
	
	
//	@ForIntent("appointment.request.yes")
//	public ActionResponse confirmAppointmentIntent(ActionRequest request) {
//
//		// Read user id from the context
//		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
//		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$
//
//		// Read date time from the context
//		context = request.getContext("slotproposed"); //$NON-NLS-1$
//		LocalDateTime dateTime = readDateTime(context.getParameters().get("dateTime")); //$NON-NLS-1$
//
//		// Read appointmentType and subject from the request
//		AppointmentType appointmentType = AppointmentType.valueOf((String) request.getParameter("appointmentType")); //$NON-NLS-1$
//		String subject = (String) request.getParameter("subject"); //$NON-NLS-1$
//
//		ResponseBuilder builder = getResponseBuilder(request);
//
//		Appointment appointment;
//		try {
//
//			appointment = appointmentService.confirmAppointment(identityNumber, dateTime, appointmentType, subject);
//
//			builder.add(AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION") //$NON-NLS-1$
//					+ renderDateTime(appointment.getDateTime()));
//			builder.removeContext("slotproposed"); //$NON-NLS-1$
//
//		} catch (UserNotFoundException e) {
//			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
//		} catch (AppointmentNotAvailableException e) {
//			builder.add(AgentResponses.getString("Responses.NO_SLOT")); //$NON-NLS-1$
//		}
//
//		ActionResponse actionResponse = builder.build();
//
//		return actionResponse;
//
//	}
//
//	@ForIntent("appointment.request.no")
//	public ActionResponse cancelAppointmentIntent(ActionRequest request) {
//
//		ResponseBuilder builder = getResponseBuilder(request);
//		builder.removeContext("slotproposed"); //$NON-NLS-1$
//		ActionResponse actionResponse = builder.build();
//
//		return actionResponse;
//
//	}
}
