package es.ja.csalud.sas.botcitas.botmanager.intents;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.response.ResponseBuilder;
import com.google.api.services.dialogflow_fulfillment.v2.model.EventInput;
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse;
import com.google.gson.internal.LinkedTreeMap;

import es.ja.csalud.sas.botcitas.botmanager.domain.model.Appointment;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.AppointmentType;

public abstract class BotManagerBaseHandler extends DialogflowApp {

	protected static final String EVENT_CANCEL_APPOINTMENT = "CANCEL_APPOINTMENT";

	protected static final String EVENT_MODIFY_APPOINTMENT = "MODIFY_APPOINTMENT";

	protected static final String EVENT_QUERY_APPOINTMENT = "QUERY_APPOINTMENT";

	protected static final String EVENT_REQUEST_APPOINTMENT = "REQUEST_APPOINTMENT";

	protected static final String EVENT_ACTIVATE_USER = "ACTIVATE_USER";

	protected static final String EVENT_CONSENT_USER = "CONSENT_USER";

	protected static final String EVENT_IDENTIFY_USER = "IDENTIFY_USER";

	protected static final String EVENT_REVOKE_USER = "REVOKE_USER";

	protected static final String EVENT_GET_USER_CLINIC = "GET_USER_CLINIC";

	protected static final String CONTEXT_LAST_USER_ACTION = "user-action";

	protected static final String CONTEXT_USER_IDENTIFIED = "user-identified";

	protected static final String CONTEXT_USER_ACTIVATED = "user-activated";

	protected static final String CONTEXT_USER_CONSENT = "user-consent";

	protected static final String CONTEXT_APPOINTMENT_CREATING = "appointment-creating";

	protected static final Integer LIFESPAN = 50;

	protected static DateTimeFormatter isoDateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

	protected static DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE;

	protected static DateTimeFormatter isoTimeFormatter = DateTimeFormatter.ISO_TIME;

	protected ActionResponse applyPreconditions(ActionRequest request, Function e, String lastActionEvent) {
		ResponseBuilder builder;

		String eventToTrigger = checkPreConditions(request);

		if (!"".equals(eventToTrigger)) { // Some precondition is not met

			builder = getResponseBuilder(request);
			triggerCustomEvent(builder, eventToTrigger);

			rememberLastActionEvent(builder, lastActionEvent);

		} else {

			builder = (ResponseBuilder) e.apply(request);
			forgetLastActionEvent(builder);

		}

		ActionResponse actionResponse = builder.build();
		return actionResponse;
	}

	protected ActionResponse dispatch(ActionRequest request, Function e) {
		ResponseBuilder builder;

		builder = (ResponseBuilder) e.apply(request);

		ActionResponse actionResponse = builder.build();
		return actionResponse;
	}

	protected void triggerLastActionEvent(ActionRequest request, ResponseBuilder builder) {

		ActionContext lastUserActionContext = request.getContext(CONTEXT_LAST_USER_ACTION); // $NON-NLS-1$
		if (lastUserActionContext != null) {
			String lastUserAction = (String) lastUserActionContext.getParameters().get("lastUserAction"); //$NON-NLS-1$
			triggerCustomEvent(builder, lastUserAction);
		}
	}

	protected void rememberLastActionEvent(ResponseBuilder builder, String lastUserAction) {
		ActionContext lastUserActionContext = new ActionContext(CONTEXT_LAST_USER_ACTION, LIFESPAN); // $NON-NLS-1$
		putObjectParameter(lastUserActionContext, "lastUserAction", lastUserAction);

		builder.add(lastUserActionContext);
	}

	protected void forgetLastActionEvent(ResponseBuilder builder) {
		builder.removeContext(CONTEXT_LAST_USER_ACTION);

	}

	protected String checkPreConditions(ActionRequest request) {

		String result = "";
		ActionContext userIdentifiedContext = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$

		if (userIdentifiedContext != null) {

			ActionContext userConsentContext = request.getContext(CONTEXT_USER_CONSENT); // $NON-NLS-1$
			if (userConsentContext != null) {

				ActionContext userActivatedContext = request.getContext(CONTEXT_USER_ACTIVATED); // $NON-NLS-1$
				if (userActivatedContext != null) {

					result = "";

				} else {
					result = EVENT_ACTIVATE_USER;
				}

			} else {
				result = EVENT_CONSENT_USER;

			}
		} else {
			result = EVENT_IDENTIFY_USER;

		}

		return result;

	}

	protected void removeAllContexts(ResponseBuilder builder) {
		builder.removeContext(CONTEXT_USER_IDENTIFIED);
		builder.removeContext(CONTEXT_USER_CONSENT);
		builder.removeContext(CONTEXT_USER_ACTIVATED);
		builder.removeContext(CONTEXT_APPOINTMENT_CREATING);
		builder.removeContext(CONTEXT_LAST_USER_ACTION);
	}

	/**
	 * Method to return in the response body the event name to be triggered in
	 * Dialogflow
	 * 
	 * @param builder
	 * @param eventName
	 */
	protected void triggerCustomEvent(ResponseBuilder builder, String eventName) {
		WebhookResponse webhookResponse = new WebhookResponse();
		EventInput eventInput = new EventInput();
		eventInput.setName(eventName);
		webhookResponse.setFollowupEventInput(eventInput);
		builder.setWebhookResponse$actions_on_google(webhookResponse);

	}

	protected String renderNextAppointment(Appointment appointment) {

		String result = AgentResponses.getString("Responses.APPOINTMENT_NEXT")
				+ renderDateTime(appointment.getDateTime());

		if (appointment.getType().equals(AppointmentType.FACE_TO_FACE)) {
			result += AgentResponses.getString("Responses.APPOINTMENT_NEXT_FACE_TO_FACE")
					+ appointment.getClinic().getName();
		} else {
			result += AgentResponses.getString("Responses.APPOINTMENT_NEXT_PHONE");

		}
		return result;

	}

	protected String renderDates(List<LocalDate> dates) {
		StringJoiner result = new StringJoiner(", ");

		for (LocalDate date : dates) {
			result.add(renderDate(date));
		}
		return result.toString();

	}

	protected String renderHours(List<LocalTime> timeSlots) {
		StringJoiner result = new StringJoiner(", ");

		for (LocalTime time : timeSlots) {
			result.add(renderTime(time));
		}
		return result.toString();
	}

	protected String renderDate(LocalDate date) {

		return "el " + date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")) + " " //$NON-NLS-1$
				+ date.getDayOfMonth() + " de "
				+ date.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"));
	}

	protected String renderTime(LocalTime time) {
		return " a las " //$NON-NLS-1$ //$NON-NLS-2$
				+ time.getHour() + " horas y " + time.getMinute() + " minutos"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String renderDateTime(LocalDateTime dateTime) {
		return renderDate(dateTime.toLocalDate()) + renderTime(dateTime.toLocalTime());
	}

	protected LocalDate readDateParameter(Object parameter) {
		LocalDate result;
		try {
			result = LocalDate.parse(readStringParameter(parameter), isoDateFormatter);

		} catch (DateTimeParseException ex) {
			result = LocalDate.parse(readStringParameter(parameter), isoDateTimeFormatter);

		}

		return result;
	}

	protected LocalTime readTimeParameter(Object parameter) {

		LocalTime result;
		try {
			result = LocalTime.parse(readStringParameter(parameter), isoTimeFormatter);

		} catch (DateTimeParseException ex) {
			result = LocalTime.parse(readStringParameter(parameter), isoDateTimeFormatter);

		}

		return adjustTime(result);
	}

	protected String readStringParameter(Object parameter) {
		String value = "";
		if (parameter instanceof LinkedTreeMap) {
			LinkedTreeMap map = (LinkedTreeMap) parameter;
			value = (String) map.values().stream().findFirst().get();
		} else if (parameter instanceof String) {
			value = (String) parameter;
		}

		return value;
	}

	/**
	 * TO DO: improve this method
	 * 
	 * @param parameter
	 * @param classType
	 * @return
	 */
	protected Enum readEnumParameter(Object parameter, Class classType) {
		return Enum.valueOf(classType, parameter.toString());

	}

	protected void putObjectParameter(ActionContext context, String parameterName, Object object) {
		Map<String, Object> params = context.getParameters();

		if (params == null) {
			params = new HashMap<String, Object>();
		}
		params.put(parameterName, object);
		context.setParameters(params);
	}

	protected void putDateParameter(ActionContext context, String parameterName, LocalDate object) {
		Map<String, Object> params = context.getParameters();

		if (params == null) {
			params = new HashMap<String, Object>();
		}

		params.put(parameterName, object.format(isoDateFormatter));
		context.setParameters(params);

	}

	protected void putTimeParameter(ActionContext context, String parameterName, LocalTime object) {
		Map<String, Object> params = context.getParameters();

		if (params == null) {
			params = new HashMap<String, Object>();
		}

		params.put(parameterName, object.format(isoTimeFormatter));
		context.setParameters(params);

	}

	/**
	 * This function changes the time if necessary to address AM/PM daily cycle,
	 * according the following mapping 1<=time<=7 -> time=time+12 8<=tim<=19 ->
	 * time=time 20<=time<=23 -> time=time-12
	 * 
	 * @param time
	 * @return
	 */
	private LocalTime adjustTime(LocalTime time) {

		if (time.getHour() <= 7) {
			return time.plusHours(12);
		} else if (time.getHour() >= 20) {
			return time.minusHours(12);
		} else
			return time;

	}

}
