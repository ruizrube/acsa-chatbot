/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.user;

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

import es.ja.csalud.sas.botcitas.botmanager.AgentResponses;
import es.ja.csalud.sas.botcitas.botmanager.DialogFlowHandler;

/**
 * @author ivanruizrube
 *
 */
@Component
public class UserIntentHandler extends DialogFlowHandler {

	@Autowired
	private UserService userService;

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

			builder.add(AgentResponses.getString("USER_NOT_FOUNDNO_USER")); //$NON-NLS-1$
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

			ActionContext userConsentContext = new ActionContext(CONTEXT_USER_CONSENT, 10); // $NON-NLS-1$
			builder.add(userConsentContext);

		} else {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
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

			// Set output context and its parameters
			ActionContext userActivatedContext = new ActionContext(CONTEXT_USER_ACTIVATED, 10); // $NON-NLS-1$
			builder.add(userActivatedContext);

		} else {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}

	/**
	 * Webhook for the followup intent when the user responds yes to revoke the
	 * usage conditions
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("user.revoke - yes")
	public ActionResponse revokeUsageConditionsFollowupIntent(ActionRequest request) {
		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityDocument = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		Optional<User> user = userService.findById(identityDocument);
		if (user.isPresent()) {
			User theUser = user.get();
			theUser.setAcceptConditions(false);
			userService.save(theUser);
			builder.add(AgentResponses.getString("Responses.USER_REVOKE_CONDITIONS")); //$NON-NLS-1$
			builder.removeContext(CONTEXT_USER_CONSENT);
		} else {
			builder.add(AgentResponses.getString("Responses.USER_NOT_FOUND")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}
}
