package es.ja.csalud.sas.botcitas.botmanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import com.google.actions.api.DialogflowApp;
import com.google.actions.api.response.ResponseBuilder;
import com.google.api.services.dialogflow_fulfillment.v2.model.EventInput;
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse;
import com.google.gson.internal.LinkedTreeMap;

public class DialogFlowHandler extends DialogflowApp{

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

	protected String renderDateTime(LocalDateTime dateTime) {
		// TODO Auto-generated method stub
		return dateTime.getDayOfMonth() + " de " //$NON-NLS-1$
				+ dateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")) + " a las " //$NON-NLS-1$ //$NON-NLS-2$
				+ dateTime.getHour() + " horas y " + dateTime.getMinute() + " minutos"; //$NON-NLS-1$ //$NON-NLS-2$
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
