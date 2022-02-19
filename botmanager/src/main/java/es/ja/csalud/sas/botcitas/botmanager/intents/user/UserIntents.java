/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.intents.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.ForIntent;

import es.ja.csalud.sas.botcitas.botmanager.intents.DialogFlowIntents;

/**
 * @author ivanruizrube
 *
 */
@Component
public class UserIntents extends DialogFlowIntents {

	@Autowired
	private IdentifyUserIntentHandler identifyUserIntentHandler;

	@Autowired
	private ConsentUserIntentHandler consentUserIntentHandler;

	@Autowired
	private ActivateUserIntentHandler activateUserIntentHandler;

	/**
	 * Webhook for the followup intent when the user tries to sign in
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("user.identify")
	public ActionResponse identifyUserIntent(ActionRequest request) {
		return this.dispatch(request, e -> identifyUserIntentHandler.identifyUser(request));

	}

	/**
	 * Webhook for the followup intent when the user responds yes to accept the
	 * usage conditions
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("user.consent - yes")
	public ActionResponse yesConsentUserIntent(ActionRequest request) {
		return this.dispatch(request, e -> consentUserIntentHandler.consentUsageConditions(request));

	}

	/**
	 * Webhook for the followup intent when the user responds yes to revoke the
	 * usage conditions
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("user.revoke - yes")
	public ActionResponse yesRevokeUserIntent(ActionRequest request) {
		return this.dispatch(request, e -> consentUserIntentHandler.revokeUsageConditions(request));

	}

	/**
	 * Webhook for activate usage conditions. This has to be extended to support
	 * some authentication method
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("user.activate - yes")
	public ActionResponse yesActivateUserIntent(ActionRequest request) {
		return this.dispatch(request, e -> activateUserIntentHandler.activateUser(request));
	}

}
