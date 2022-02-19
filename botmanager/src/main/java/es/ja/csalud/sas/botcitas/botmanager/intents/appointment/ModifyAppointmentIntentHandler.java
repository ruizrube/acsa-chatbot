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
public class ModifyAppointmentIntentHandler extends DialogFlowIntents {

	@Autowired
	private AppointmentService appointmentService;

	public ResponseBuilder modifyAppointment(ActionRequest request) {

		// Read identityNumber from context
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); 
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); 

		ResponseBuilder builder = getResponseBuilder(request);

		Optional<Appointment> appointmentOpt;
		try {
			appointmentOpt = appointmentService.findNextAppointment(userIdentifier);

			if (appointmentOpt.isPresent()) {

				triggerCustomEvent(builder, EVENT_REQUEST_APPOINTMENT);

			} else {
				builder.add(AgentResponses.getString("Responses.APPOINTMENT_NO") 
				);
			}

		} catch (UserNotFoundException e) {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); 
		}

		return builder;

	}

}
