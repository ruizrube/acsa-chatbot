package es.ja.csalud.sas.botcitas.botmanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.gson.internal.LinkedTreeMap;

import es.ja.csalud.sas.botcitas.botmanager.appoinment.Appointment;
import es.ja.csalud.sas.botcitas.botmanager.appoinment.AppointmentNotAvailableException;
import es.ja.csalud.sas.botcitas.botmanager.appoinment.AppointmentService;
import es.ja.csalud.sas.botcitas.botmanager.appoinment.AppointmentType;
import es.ja.csalud.sas.botcitas.botmanager.user.User;
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotFoundException;
import es.ja.csalud.sas.botcitas.botmanager.user.UserService;

@Component
public class DialogFlowIntents extends DialogflowApp {

	private DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE_TIME;

	@Autowired
	private UserService userService;
	@Autowired
	private AppointmentService appointmentService;

	@ForIntent("user.identify")
	public ActionResponse identificateUserIntent(ActionRequest request) {

		// Read request parameter
		String identityDocument = (String) request.getParameter("identityDocument"); //$NON-NLS-1$

		Optional<User> user = userService.findById(identityDocument);
		ResponseBuilder builder;
		if (user.isPresent()) {

			// Write response
			builder = getResponseBuilder(request);
			builder.add(AgentResponses.getString("Responses.GREETING") + user.get().getName() //$NON-NLS-1$
					+ AgentResponses.getString("Responses.HELP")); //$NON-NLS-1$

			// Set output context and its parameters
			ActionContext context = new ActionContext("ctx-useridentified", 10); //$NON-NLS-1$
			Map<String, String> params = new HashMap<String, String>();
			params.put("identityDocument", user.get().getIdentityDocument()); //$NON-NLS-1$
			params.put("userName", user.get().getFirstName()); //$NON-NLS-1$
			context.setParameters(params);
			builder.add(context);

		} else {

			builder = getResponseBuilder(request);
			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();

		return actionResponse;
	}

	@ForIntent("appointment.query")
	public ActionResponse rememberAppointmentIntent(ActionRequest request) {

		// Read context parameter
		ActionContext context = request.getContext("ctx-useridentified"); //$NON-NLS-1$
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		ResponseBuilder builder;

		try {
			Optional<Appointment> appointmentOpt = appointmentService.findNextAppointment(identityNumber);

			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();
				builder = getResponseBuilder(request);
				builder.add(
						AgentResponses.getString("Responses.NEXT_APPOINTMENT") + renderDateTime(appointment.getDateTime())); //$NON-NLS-1$

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

	@ForIntent("appointment.request")
	public ActionResponse queryAvaliabilityIntent(ActionRequest request) {

		LocalDateTime slot = appointmentService.findNextAvailableSlot(AppointmentType.FACE_TO_FACE);

		ResponseBuilder builder = getResponseBuilder(request);

		builder.add(AgentResponses.getString("Responses.NEXT_SLOT") + renderDateTime(slot) //$NON-NLS-1$
				+ AgentResponses.getString("Responses.CONFIRM_APPOINTMENT")); //$NON-NLS-1$

		// Set output context
		ActionContext context = new ActionContext("ctx-slotproposed", 5); //$NON-NLS-1$
		Map<String, String> params = new HashMap<String, String>();
		params.put("dateTime", slot.format(isoDateFormatter)); //$NON-NLS-1$
		context.setParameters(params);
		builder.add(context);

		ActionResponse actionResponse = builder.build();
		return actionResponse;

	}

	@ForIntent("appointment.request.yes")
	public ActionResponse confirmAppointmentIntent(ActionRequest request) {

		// Read user id from the context
		ActionContext context = request.getContext("ctx-useridentified"); //$NON-NLS-1$
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		// Read date time from the context
		context = request.getContext("ctx-slotproposed"); //$NON-NLS-1$
		LocalDateTime dateTime = readDateTime(context.getParameters().get("dateTime")); //$NON-NLS-1$

		// Read appointmentType and subject from the request
		AppointmentType appointmentType = AppointmentType.valueOf((String) request.getParameter("appointmentType")); //$NON-NLS-1$
		String subject = (String) request.getParameter("subject"); //$NON-NLS-1$

		ResponseBuilder builder = getResponseBuilder(request);

		Appointment appointment;
		try {

			appointment = appointmentService.confirmAppointment(identityNumber, dateTime, appointmentType, subject);

			builder.add(AgentResponses.getString("Responses.APPOINTMENT_CONFIRMATION") //$NON-NLS-1$
					+ renderDateTime(appointment.getDateTime()));
			builder.removeContext("ctx-slotproposed"); //$NON-NLS-1$

		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
		} catch (AppointmentNotAvailableException e) {
			builder.add(AgentResponses.getString("Responses.NO_SLOT")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}

	@ForIntent("appointment.request.no")
	public ActionResponse cancelAppointmentIntent(ActionRequest request) {

		ResponseBuilder builder = getResponseBuilder(request);
		builder.removeContext("ctx-slotproposed"); //$NON-NLS-1$
		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}

	private String renderDateTime(LocalDateTime dateTime) {
		// TODO Auto-generated method stub
		return dateTime.getDayOfMonth() + " de " //$NON-NLS-1$
				+ dateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")) + " a las " //$NON-NLS-1$ //$NON-NLS-2$
				+ dateTime.getHour() + " horas y " + dateTime.getMinute() + " minutos"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private LocalDateTime readDateTime(Object parameter) {
		String value = ""; //$NON-NLS-1$
		if (parameter instanceof LinkedTreeMap) {
			LinkedTreeMap map = (LinkedTreeMap) parameter;
			value = (String) map.values().stream().findFirst().get();
		} else if (parameter instanceof String) {
			value = (String) parameter;
		}

		return LocalDateTime.parse(value, isoDateFormatter);
	}

}