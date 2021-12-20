/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.clinic;

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
import es.ja.csalud.sas.botcitas.botmanager.user.User;
import es.ja.csalud.sas.botcitas.botmanager.user.UserService;

/**
 * @author ivanruizrube
 *
 */
@Component
public class ClinicIntentHandler extends DialogFlowHandler {

	@Autowired
	private UserService userService;

	@Autowired
	private ClinicService clinicService;

	/**
	 * Webhook for querying the user's clinic
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("general.clinic")
	public ActionResponse retrieveUserClinic(ActionRequest request) {

		ResponseBuilder builder = getResponseBuilder(request);

		// Read context parameter
		ActionContext context = request.getContext(CONTEXT_USER_IDENTIFIED); // $NON-NLS-1$
		String identityDocument = (String) context.getParameters().get("identityDocument"); //$NON-NLS-1$

		Optional<User> user = userService.findById(identityDocument);
		if (user.isPresent()) {
			Optional<Clinic> clinic = user.get().getClinic();
			if (clinic.isPresent()) {
				Clinic theClinic = clinic.get();

				String result = AgentResponses.getString("Responses.USER_CLINIC_1") + theClinic.getName()
						+ AgentResponses.getString("Responses.USER_CLINIC_2") + theClinic.getAddress()
						+ AgentResponses.getString("Responses.USER_CLINIC_3") + theClinic.getPhone();
				builder.add(result); // $NON-NLS-1$

			} else {
				builder.add(AgentResponses.getString("Responses.USER_HAS_NOT_CLINIC")); //$NON-NLS-1$

			}

		} else {
			builder.add(AgentResponses.getString("Responses.NO_USER")); //$NON-NLS-1$
		}

		ActionResponse actionResponse = builder.build();

		return actionResponse;

	}
}
