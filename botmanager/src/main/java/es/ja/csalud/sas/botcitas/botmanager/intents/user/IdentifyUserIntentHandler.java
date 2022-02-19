/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.intents.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;

import es.ja.csalud.sas.botcitas.botmanager.domain.exceptions.UserNotFoundException;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.Appointment;
import es.ja.csalud.sas.botcitas.botmanager.domain.model.User;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.AppointmentService;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.UserService;
import es.ja.csalud.sas.botcitas.botmanager.intents.AgentResponses;
import es.ja.csalud.sas.botcitas.botmanager.intents.DialogFlowIntents;

/**
 * @author ivanruizrube
 *
 */
@Component
public class IdentifyUserIntentHandler extends DialogFlowIntents {

	@Autowired
	private UserService userService;

	
	
	public ResponseBuilder identifyUser(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read request parameter
		String identifier = (String) request.getParameter("userIdentifier"); 

		removeAllContexts(builder);

		Optional<User> user = userService.findByIdentifier(identifier);
		if (user.isPresent()) {

			User theUser = user.get();

			ActionContext userIdentifiedContext = new ActionContext(CONTEXT_USER_IDENTIFIED, LIFESPAN); 
			Map<String, String> params = new HashMap<String, String>();
			params.put("userIdentifier", identifier); 
			params.put("userName", user.get().getFirstName()); 
			userIdentifiedContext.setParameters(params);

			builder.add(userIdentifiedContext);

			if (theUser.isAcceptConditions()) {
				ActionContext userConsentContext = new ActionContext(CONTEXT_USER_CONSENT, LIFESPAN); 
				builder.add(userConsentContext);
			}

			if (theUser.isEnabled()) {
				ActionContext userActivatedContext = new ActionContext(CONTEXT_USER_ACTIVATED, LIFESPAN); 
				builder.add(userActivatedContext);

				// Write response
				builder.add(AgentResponses.getString("Responses.GREETING_1") + theUser.getName() + ", " 
						+ AgentResponses.getString("Responses.GREETING_2")); 
				
				triggerLastActionEvent(request, builder);


			} else {
				// Write response
				builder.add(AgentResponses.getString("Responses.GREETING_1") // por seguridad no se revela el nombre del
																				// usuario
						+ AgentResponses.getString("Responses.GREETING_2")); 
				
				triggerLastActionEvent(request, builder);

			}

		} else {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); 
		}

		
		return builder;

	}


}
