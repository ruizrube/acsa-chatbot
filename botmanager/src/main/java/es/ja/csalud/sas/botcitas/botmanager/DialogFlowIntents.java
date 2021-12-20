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
import com.google.api.services.dialogflow_fulfillment.v2.model.EventInput;
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse;
import com.google.gson.internal.LinkedTreeMap;

import es.ja.csalud.sas.botcitas.botmanager.appoinment.Appointment;
import es.ja.csalud.sas.botcitas.botmanager.appoinment.AppointmentNotAvailableException;
import es.ja.csalud.sas.botcitas.botmanager.appoinment.AppointmentService;
import es.ja.csalud.sas.botcitas.botmanager.appoinment.AppointmentType;
import es.ja.csalud.sas.botcitas.botmanager.clinic.Clinic;
import es.ja.csalud.sas.botcitas.botmanager.clinic.ClinicService;
import es.ja.csalud.sas.botcitas.botmanager.user.User;
import es.ja.csalud.sas.botcitas.botmanager.user.UserNotFoundException;
import es.ja.csalud.sas.botcitas.botmanager.user.UserService;

@Component
public class DialogFlowIntents extends DialogflowApp {

	private static final String EVENT_CONSENT = "CONSENT";

	private static final String EVENT_ACTIVATE = "ACTIVATE";

	private static final String EVENT_REQUEST_APPOINTMENT = "REQUEST_APPOINTMENT";

	private static final String CONTEXT_USER_IDENTIFIED = "user-identified";

	private static final String CONTEXT_USER_ACTIVATED = "user-activated";

	private static final String CONTEXT_USER_CONSENT = "user-consent";

	private DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE_TIME;

	@Autowired
	private UserService userService;
	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private ClinicService clinicService;

	/**
	 * Webhook for the intention of identify the user
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("user.identify")
	public ActionResponse identificateUserIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read request parameter
		String identityDocument = (String) request.getParameter("identityDocument"); //$NON-NLS-1$

		Optional<User> user = userService.findById(identityDocument);
		if (user.isPresent()) {

			ActionContext userIdentifiedContext = new ActionContext(CONTEXT_USER_IDENTIFIED, 10); // $NON-NLS-1$
			Map<String, String> params = new HashMap<String, String>();
			params.put("identityDocument", user.get().getIdentityDocument()); //$NON-NLS-1$
			params.put("userName", user.get().getFirstName()); //$NON-NLS-1$
			userIdentifiedContext.setParameters(params);

			builder.add(userIdentifiedContext);

			if (user.get().isEnabled()) {

				// Set output context and its parameters
				ActionContext userActivatedContext = new ActionContext(CONTEXT_USER_ACTIVATED, 10); // $NON-NLS-1$
				builder.add(userActivatedContext);

				if (user.get().isAcceptConditions()) {
					// Write response
					builder.add(AgentResponses.getString("Responses.GREETING_1") + user.get().getName() //$NON-NLS-1$
							+ AgentResponses.getString("Responses.GREETING_2")); //$NON-NLS-1$

					ActionContext userConsentContext = new ActionContext(CONTEXT_USER_CONSENT, 10); // $NON-NLS-1$
					builder.add(userConsentContext);

				} else {
					builder.add(AgentResponses.getString("Responses.USER_NOT_ACCEPTED_CONDITIONS")); //$NON-NLS-1$

				}

			} else {
				builder.add(AgentResponses.getString("Responses.USER_NOT_ENABLED")); //$NON-NLS-1$
			}

		} else {

			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
			builder.setExpectUserResponse$actions_on_google(false);
		}

		ActionResponse actionResponse = builder.build();
		return actionResponse;
	}

	/**
	 * Webhook for the followup intent when the user responds yes to the oportunity
	 * to activate him/herself or accept the usage conditions
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("user.identify - yes")
	public ActionResponse identificateUserFollowupIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter

		ActionContext userActivatedContext = request.getContext(CONTEXT_USER_ACTIVATED); // $NON-NLS-1$
		if (userActivatedContext != null) { // Si el usuario está activo entonces lo que quiere es aceptar las
											// condiciones de uso
			triggerCustomEvent(builder, EVENT_CONSENT);

		} else { // Si no está activo, entonces lo que quiere es activarlo
			triggerCustomEvent(builder, EVENT_ACTIVATE);
		}

		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}

	/**
	 * Webhook for the followup intent when the user responds yes to accept the
	 * usage conditions
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("user.consent - yes")
	public ActionResponse consentUsageConditionsFollowupIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityDocument = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		Optional<User> user = userService.findById(identityDocument);
		if (user.isPresent()) {
			User theUser = user.get();
			theUser.setAcceptConditions(true);
			userService.save(theUser);
			builder.add(AgentResponses.getString("Responses.USER_ACCEPTED_CONDITIONS")); //$NON-NLS-1$
		} else {
			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}

	/**
	 * Webhook for activate usage conditions. This has to be extended to support
	 * some authentication method
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("user.activate")
	public ActionResponse activateUserIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityDocument = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		Optional<User> user = userService.findById(identityDocument);
		if (user.isPresent()) {
			User theUser = user.get();
			theUser.setEnabled(true);
			userService.save(theUser);
			builder.add(AgentResponses.getString("Responses.USER_ACTIVATED")); //$NON-NLS-1$
		} else {
			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}

	/**
	 * Webhook for querying the user's clinic
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("general.clinic")
	public ActionResponse retrieveUserClinic(ActionRequest request) {

		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityDocument = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		Optional<User> user = userService.findById(identityDocument);
		if (user.isPresent()) {
			Optional<Clinic> clinic = user.get().getClinic();
			if (clinic.isPresent()) {
				Clinic theClinic = clinic.get();

				String result = AgentResponses.getString("Responses.USER_CLINIC_1") + theClinic.getName()
						+ AgentResponses.getString("Responses.USER_CLINIC_2") + theClinic.getAddress()
						+ AgentResponses.getString("Responses.USER_CLINIC_3") + theClinic.getPhone();
				builder.add(result); // $NON-NLS-1$

			} else {
				builder.add(AgentResponses.getString("Responses.USER_HAS_NOT_CLINIC")); //$NON-NLS-1$

			}

		} else {
			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}

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
	 * Webhook for the followup intent when the user responds yes to request for a new appointment
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

	@ForIntent("appointment.request")
	public ActionResponse queryAvaliabilityIntent(ActionRequest request) {

		LocalDateTime slot = appointmentService.findNextAvailableSlot(AppointmentType.FACE_TO_FACE);

		ResponseBuilder builder = getResponseBuilder(request);

		builder.add(AgentResponses.getString("Responses.NEXT_SLOT") + renderDateTime(slot) //$NON-NLS-1$
				+ AgentResponses.getString("Responses.CONFIRM_APPOINTMENT")); //$NON-NLS-1$

		// Set output context
		ActionContext context = new ActionContext("slotproposed", 5); //$NON-NLS-1$
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
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityNumber = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		// Read date time from the context
		context = request.getContext("slotproposed"); //$NON-NLS-1$
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
			builder.removeContext("slotproposed"); //$NON-NLS-1$

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
		builder.removeContext("slotproposed"); //$NON-NLS-1$
		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}

	/**
	 * Method to return in the response body the event name to be triggered in
	 * Dialogflow
	 * 
	 * @param builder
	 * @param eventName
	 */
	private void triggerCustomEvent(ResponseBuilder builder, String eventName) {
		WebhookResponse webhookResponse = new WebhookResponse();
		EventInput eventInput = new EventInput();
		eventInput.setName(eventName);
		webhookResponse.setFollowupEventInput(eventInput);
		builder.setWebhookResponse$actions_on_google(webhookResponse);

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