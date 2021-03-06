/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.intents.clinic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.ForIntent;

import es.ja.csalud.sas.botcitas.botmanager.intents.BotManagerBaseHandler;

/**
 * @author ivanruizrube
 *
 */
@Component
public class ClinicIntents extends BotManagerBaseHandler {

	@Autowired
	private GetUserClinicIntentHandler getUserClinicIntentHandler;

	/**
	 * Webhook for querying the user's clinic
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("commons.getUserClinic")
	public ActionResponse getUserClinic(ActionRequest request) {
		return this.applyPreconditions(request, e -> getUserClinicIntentHandler.getUserClinic(request),EVENT_GET_USER_CLINIC);

	}

}
