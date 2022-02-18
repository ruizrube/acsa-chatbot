/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.user.intenthandlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;

import es.ja.csalud.sas.botcitas.botmanager.AgentResponses;
import es.ja.csalud.sas.botcitas.botmanager.DialogFlowIntents;
import es.ja.csalud.sas.botcitas.botmanager.user.UserService;
import es.ja.csalud.sas.botcitas.botmanager.user.domain.User;

/**
 * @author ivanruizrube
 *
 */
@Component
public class ActivateUserIntentHandler extends DialogFlowIntents {

	@Autowired
	private UserService userService;

		
	public ResponseBuilder activateUser(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); 
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); 

		Optional<User> user = userService.findByIdentifier(userIdentifier);
		if (user.isPresent()) {
			User theUser = user.get();
			theUser.setEnabled(true);
			userService.save(theUser);

			// Set output context and its parameters
			ActionContext userActivatedContext = new ActionContext(CONTEXT_USER_ACTIVATED, LIFESPAN); 
			builder.add(userActivatedContext);

			builder.add(AgentResponses.getString("Responses.USER_ACTIVATED")); 
			
			triggerLastActionEvent(request, builder);


		} else {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); 
		}

		return builder;

	}
	
}
