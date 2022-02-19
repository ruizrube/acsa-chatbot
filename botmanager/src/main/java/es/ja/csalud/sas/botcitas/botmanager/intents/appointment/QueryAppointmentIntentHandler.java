/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.intents.appointment;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.response.ResponseBuilder;

import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.UserNotFoundException;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.Appointment;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.AppointmentService;
import es.ja.csalud.sas.botcitas.botmanager.intents.AgentResponses;
import es.ja.csalud.sas.botcitas.botmanager.intents.DialogFlowIntents;

/**
 * @author ivanruizrube
 *
 */
@Component
public class QueryAppointmentIntentHandler extends DialogFlowIntents {

	@Autowired
	private AppointmentService appointmentService;

	

	public ResponseBuilder queryAppointment(ActionRequest request) {

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

		return builder;

	}

	

	public ResponseBuilder requestNewAppointment(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// El usuario no tenia cita asignada y quiere pedir una...
		triggerCustomEvent(builder, EVENT_REQUEST_APPOINTMENT);

		return builder;
	}

}
