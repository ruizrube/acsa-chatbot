/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.intents.appointment;

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

import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.AppointmentNotAvailableException;
import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.UserNotAssignedToClinicException;
import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.UserNotAssignedToDoctorException;
import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.UserNotFoundException;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.Appointment;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.AppointmentType;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.AppointmentService;
import es.ja.csalud.sas.botcitas.botmanager.intents.AgentResponses;
import es.ja.csalud.sas.botcitas.botmanager.intents.BotManagerBaseHandler;

/**
 * @author ivanruizrube
 *
 */
@Component
public class RequestAppointmentIntentHandler extends BotManagerBaseHandler {

	@Autowired
	private AppointmentService appointmentService;

	public ResponseBuilder requestAppointment(ActionRequest request) {

		
		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED);
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		ResponseBuilder builder = getResponseBuilder(request);

		Optional<Appointment> appointmentOpt;
		try {
			
			// Comprobamos si el usuario tiene ya una cita solicitada
			appointmentOpt = appointmentService.findNextAppointment(userIdentifier);

			String preTextResponse = "";
			if (appointmentOpt.isPresent()) {
				preTextResponse = AgentResponses.getString("Responses.APPOINTMENT_ALREADY") //$NON-NLS-1$
						+ renderNextAppointment(appointmentOpt.get());

			}

			ActionContext creatingAppointmentContext = new ActionContext(CONTEXT_APPOINTMENT_CREATING, LIFESPAN);

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

		return builder;

	}

	public ResponseBuilder rejectDaysProposed(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED);
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING);

		proposeDatesToUser(userIdentifier, builder, creatingAppointmentContext, "");

		return builder;
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

				StringBuffer sb=new StringBuffer();
				sb.append(preTextResponse);
				if(preTextResponse.length()>0) {
					sb.append(". ");
				}
				sb.append(AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_1"));
				sb.append(renderDates(availableDays));
				sb.append(AgentResponses.getString("Responses.AVAILABLE_DAY_SLOTS_2"));
				builder.add(sb.toString());
			} else {
				
				StringBuffer sb=new StringBuffer();
				sb.append(preTextResponse);
				if(preTextResponse.length()>0) {
					sb.append(". ");
				}
				sb.append(AgentResponses.getString("Responses.AVAILABLE_DAY_NO"));
				builder.add(sb.toString());
				builder.removeContext(CONTEXT_APPOINTMENT_CREATING);
			}
		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); 
		} catch (UserNotAssignedToDoctorException e) {
			builder.add(AgentResponses.getString("Responses.USER_NO_ASSIGNED_TO_DOCTOR")); 
		} catch (UserNotAssignedToClinicException e) {
			builder.add(AgentResponses.getString("Responses.USER_NO_ASSIGNED_TO_CLINIC")); 
		}
	}

	public ResponseBuilder acceptDayProposed(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED);
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		// Read date from request
		LocalDate date = readDateParameter(request.getParameter("date")); //$NON-NLS-1$

		// Write date into context
		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING);
		putDateParameter(creatingAppointmentContext, "date", date);

		// Write last time proposed into context
		putTimeParameter(creatingAppointmentContext, "lastTimeProposed", LocalTime.of(8, 0));

		proposeHoursToUser(userIdentifier, builder, creatingAppointmentContext);

		return builder;

	}

	public ResponseBuilder rejectHoursProposed(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED);
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING);

		proposeHoursToUser(userIdentifier, builder, creatingAppointmentContext);

		return builder;
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

					builder.add("Para " + renderDate(date) + ", "
							+ AgentResponses.getString("Responses.AVAILABLE_HOURS_SLOTS_1") //$NON-NLS-1$
							+ renderHours(availableHours)
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

	public ResponseBuilder acceptHourProposed(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read date from context
		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING);
		LocalDate date = readDateParameter(creatingAppointmentContext.getParameters().get("date")); //$NON-NLS-1$

		// Read time from request
		LocalTime time = readTimeParameter(request.getParameter("time")); //$NON-NLS-1$

		// Write time into context
		putTimeParameter(creatingAppointmentContext, "time", time);

		LocalDateTime slotProposed = LocalDateTime.of(date, time);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED);
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		Optional<Appointment> appointmentOpt;
		try {
			appointmentOpt = appointmentService.findNextAppointment(userIdentifier);

			StringBuffer sb=new StringBuffer();
					
			if (appointmentOpt.isPresent()) {
				sb.append(AgentResponses.getString("Responses.APPOINTMENT_ALREADY_1") );
				sb.append(renderNextAppointment(appointmentOpt.get()));
				sb.append(". ");
				sb.append(AgentResponses.getString("Responses.APPOINTMENT_ALREADY_2"));
				sb.append(". ");				
			}
			
			sb.append(AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION_1"));
			sb.append(renderDateTime(slotProposed));
			sb.append(AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION_2"));
			builder.add(sb.toString());

		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		}

		return builder;

	}

	public ResponseBuilder confirmAppointment(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED);
		String userIdentifier = (String) context.getParameters().get("userIdentifier");

		// Read date and time from context
		ActionContext creatingAppointmentContext = request.getContext(CONTEXT_APPOINTMENT_CREATING);
		LocalDate date = readDateParameter(creatingAppointmentContext.getParameters().get("date"));
		LocalTime time = readTimeParameter(creatingAppointmentContext.getParameters().get("time"));
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
			builder.add(AgentResponses.getString("Responses.AVAILABLE_NO_SLOT")); //$NON-NLS-1$
		}

		return builder;

	}

}
