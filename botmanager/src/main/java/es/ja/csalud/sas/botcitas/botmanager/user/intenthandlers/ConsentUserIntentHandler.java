/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.user.intenthandlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionContext;
import com.google.actions.api.ActionRequest;
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
public class ConsentUserIntentHandler extends DialogFlowIntents {

	@Autowired
	private UserService userService;

	public ResponseBuilder consentUsageConditions(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED);
		String userIdentifier = (String) context.getParameters().get("userIdentifier"); 

		Optional<User> user = userService.findByIdentifier(userIdentifier);
		if (user.isPresent()) {
			User theUser = user.get();
			theUser.setAcceptConditions(true);
			userService.save(theUser);

			// Set output context and its parameters
			ActionContext userConsentContext = new ActionContext(CONTEXT_USER_CONSENT, LIFESPAN);
			builder.add(userConsentContext);

			builder.add(AgentResponses.getString("Responses.USER_ACCEPTED_CONDITIONS")); 

			triggerLastActionEvent(request, builder);

		} else {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); 
		}

		return builder;

	}

	
	public ResponseBuilder revokeUsageConditions(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED);
		String userIdentifier = (String) context.getParameters().get("userIdentifier");

		Optional<User> user = userService.findByIdentifier(userIdentifier);
		if (user.isPresent()) {
			User theUser = user.get();
			theUser.setAcceptConditions(false);
			userService.save(theUser);
			builder.add(AgentResponses.getString("Responses.USER_REVOKE_CONDITIONS"));
			builder.removeContext(CONTEXT_USER_CONSENT);
		} else {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); 
		}

		return builder;

	}

}
