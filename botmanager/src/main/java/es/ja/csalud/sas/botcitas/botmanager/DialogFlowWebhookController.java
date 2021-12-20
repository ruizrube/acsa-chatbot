package es.ja.csalud.sas.botcitas.botmanager;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.ja.csalud.sas.botcitas.botmanager.appoinment.AppointmentIntentHandler;
import es.ja.csalud.sas.botcitas.botmanager.clinic.ClinicIntentHandler;
import es.ja.csalud.sas.botcitas.botmanager.user.UserIntentHandler;

@RestController
public class DialogFlowWebhookController {

	// private static Logger logger =
	// LoggerFactory.getLogger(DialogFlowWebhookController.class);

	@Autowired
	private AppointmentIntentHandler appointmentIntentHandler;

	@Autowired
	private UserIntentHandler userIntentHandler;

	@Autowired
	private ClinicIntentHandler clinicIntentHandler;

	@RequestMapping(value = "/dialogflow", method = RequestMethod.POST, produces = { "application/json" })
	String serveAction(@RequestBody String body, @RequestHeader Map<String, String> headers) {

		try {
			return appointmentIntentHandler.handleRequest(body, headers).get();
		} catch (InterruptedException | ExecutionException e) {
			try {
				return userIntentHandler.handleRequest(body, headers).get();
			} catch (InterruptedException | ExecutionException e1) {
				try {
					return clinicIntentHandler.handleRequest(body, headers).get();
				} catch (InterruptedException | ExecutionException e2) {
					return handleError(e);
				}
			}

		}

	}

	private String handleError(Exception e) {
		e.printStackTrace();
		// logger.error("Error in App.handleRequest ", e);
		return "Error handling the intent - " + e.getMessage();
	}

}