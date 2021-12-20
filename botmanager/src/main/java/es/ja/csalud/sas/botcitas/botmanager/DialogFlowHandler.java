package es.ja.csalud.sas.botcitas.botmanager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import com.google.actions.api.DialogflowApp;
import com.google.actions.api.response.ResponseBuilder;
import com.google.api.services.dialogflow_fulfillment.v2.model.EventInput;
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse;
import com.google.gson.internal.LinkedTreeMap;

public class DialogFlowHandler extends DialogflowApp {

	protected static final String EVENT_CONSENT = "CONSENT";

	protected static final String EVENT_ACTIVATE = "ACTIVATE";

	protected static final String EVENT_REQUEST_APPOINTMENT = "REQUEST_APPOINTMENT";

	protected static final String CONTEXT_USER_IDENTIFIED = "user-identified";

	protected static final String CONTEXT_USER_ACTIVATED = "user-activated";

	protected static final String CONTEXT_USER_CONSENT = "user-consent";

	protected DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE_TIME;

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

	protected String renderDates() {
		// TODO Auto-generated method stub
		return "el lunes 1 por la mañana, 2 por la mañana, 3 por la tarde";
	}

	protected String renderHours() {
		// TODO Auto-generated method stub
		return "el lunes 1 a las 08:00, 08:10, 08:20, más tarde u otra fecha, ¿cuál prefieres?";
	}

	protected String renderDate(LocalDate date) {
		return "el" + date.getDayOfMonth() + " de " //$NON-NLS-1$
				+ date.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"));
	}

	protected String renderTime(LocalTime time) {
		return " a las " //$NON-NLS-1$ //$NON-NLS-2$
				+ time.getHour() + " horas y " + time.getMinute() + " minutos"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String renderDateTime(LocalDateTime dateTime) {
		return renderDate(dateTime.toLocalDate()) + renderTime(dateTime.toLocalTime());
	}

	protected LocalDateTime readDateTime(Object parameter) {
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
