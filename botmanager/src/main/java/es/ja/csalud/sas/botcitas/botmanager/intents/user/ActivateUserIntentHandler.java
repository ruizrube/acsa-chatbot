/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.intents.user;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.response.ResponseBuilder;

import es.ja.csalud.sas.botcitas.botmanager.domain.model.User;
import es.ja.csalud.sas.botcitas.botmanager.domain.services.UserService;
import es.ja.csalud.sas.botcitas.botmanager.intents.AgentResponses;
import es.ja.csalud.sas.botcitas.botmanager.intents.BotManagerBaseHandler;

/**
 * @author ivanruizrube
 *
 */
@Component
public class ActivateUserIntentHandler extends BotManagerBaseHandler {

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
