/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.intents.clinic;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.response.ResponseBuilder;

import es.ja.csalud.sas.botcitas.botmanager.domain.model.Clinic;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.User;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.UserService;
import es.ja.csalud.sas.botcitas.botmanager.intents.AgentResponses;
import es.ja.csalud.sas.botcitas.botmanager.intents.DialogFlowIntents;

/**
 * @author ivanruizrube
 *
 */
@Component
public class GetUserClinicIntentHandler extends DialogFlowIntents {

	@Autowired
	private UserService userService;

	
	public ResponseBuilder getUserClinic(ActionRequest request) {

		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); //$NON-NLS-1$

		Optional<User> user = userService.findByIdentifier(userIdentifier);
		if (user.isPresent()) {
			Optional<Clinic> clinic = user.get().getClinic();
			if (clinic.isPresent()) {
				Clinic theClinic = clinic.get();

				String result = AgentResponses.getString("Responses.USER_CLINIC_1") + theClinic.getName()
						+ AgentResponses.getString("Responses.USER_CLINIC_2") + theClinic.getAddress()
						+ AgentResponses.getString("Responses.USER_CLINIC_3") + theClinic.getPhone();
				builder.add(result);

			} else {
				builder.add(AgentResponses.getString("Responses.USER_NO_ASSIGNED_TO_CLINIC"));

			}

		} else {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND"));
		}

		
		return builder;

	}

}
