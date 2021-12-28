package es.ja.csalud.sas.botcitas.botmanager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import com.google.actions.api.ActionContext;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.response.ResponseBuilder;
import com.google.api.services.dialogflow_fulfillment.v2.model.EventInput;
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse;
import com.google.gson.internal.LinkedTreeMap;

public class DialogFlowHandler extends DialogflowApp {

	protected static final String EVENT_SIGN_IN = "SIGN_IN";
	
	protected static final String EVENT_CONSENT = "CONSENT";

	protected static final String EVENT_ACTIVATE = "ACTIVATE";

	protected static final String EVENT_REQUEST_APPOINTMENT = "REQUEST_APPOINTMENT";

	protected static final String CONTEXT_USER_IDENTIFIED = "user-identified";

	protected static final String CONTEXT_USER_ACTIVATED = "user-activated";

	protected static final String CONTEXT_USER_CONSENT = "user-consent";

	protected static final String CONTEXT_APPOINTMENT_CREATING = "appointment-creating";

	protected DateTimeFormatter isoDateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

	protected DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE;

	private DateTimeFormatter isoTimeFormatter = DateTimeFormatter.ISO_TIME;

	
	protected void removeAllContexts(ResponseBuilder builder) {
		builder.removeContext(CONTEXT_USER_IDENTIFIED);
		builder.removeContext(CONTEXT_USER_CONSENT);
		builder.removeContext(CONTEXT_USER_ACTIVATED);
		builder.removeContext(CONTEXT_APPOINTMENT_CREATING);
		
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
		return "el " + date.getDayOfMonth() + " de " //$NON-NLS-1$
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

		return LocalDate.parse(readStringParameter(parameter), isoDateTimeFormatter);
	}

	protected LocalTime readTimeParameter(Object parameter) {

		return LocalTime.parse(readStringParameter(parameter), isoDateTimeFormatter);
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
	 * @param parameter
	 * @param classType
	 * @return
	 */
	protected Enum readEnumParameter(Object parameter, Class classType) {
		return Enum.valueOf(classType, parameter.toString());
		
	}
	protected void putParameter(ActionContext context, String parameterName, Object object) {
		Map<String, Object> params = context.getParameters();

		if (params == null) {
			params = new HashMap<String, Object>();
		}
		params.put(parameterName, object);
		context.setParameters(params);


	}
}
