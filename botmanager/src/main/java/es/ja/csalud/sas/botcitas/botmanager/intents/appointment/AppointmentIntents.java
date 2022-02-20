/**
 * 
 */
package es.ja.csalud.sas.botcitas.botmanager.intents.appointment;

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
public class AppointmentIntents extends BotManagerBaseHandler {

	@Autowired
	private QueryAppointmentIntentHandler queryAppointmentIntentHandler;

	@Autowired
	private ModifyAppointmentIntentHandler modifyAppointmentIntentHandler;

	@Autowired
	private CancelAppointmentIntentHandler cancelAppointmentIntentHandler;

	@Autowired
	private RequestAppointmentIntentHandler requestAppointmentIntentHandler;

	/**
	 * Webhook for requesting a new appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request")
	public ActionResponse requestAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> requestAppointmentIntentHandler.requestAppointment(request),EVENT_REQUEST_APPOINTMENT);
		
	}
	
	
	/**
	 * Webhook for the followup intent when the user rejected the days proposed
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.otherday")
	public ActionResponse otherdayRequestAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> requestAppointmentIntentHandler.rejectDaysProposed(request),EVENT_REQUEST_APPOINTMENT);
		
	}
	
	
	/**
	 * Webhook for the followup intent when the user has to select the day
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.dayselection")
	public ActionResponse dayselectionRequestAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> requestAppointmentIntentHandler.acceptDayProposed(request),EVENT_REQUEST_APPOINTMENT);
		
	}
	
	
	/**
	 * Webhook for the followup intent when the user rejected the hours proposed
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.dayselection.otherhour")
	public ActionResponse otherHourDaySelectionRequestAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> requestAppointmentIntentHandler.rejectHoursProposed(request),EVENT_REQUEST_APPOINTMENT);	
	}
	
	
	
	
	
	/**
	 * Webhook for the followup intent when the user has to select the hour
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.dayselection.hourselection")
	public ActionResponse hourSelectionDaySelectionRequestAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> requestAppointmentIntentHandler.acceptHourProposed(request),EVENT_REQUEST_APPOINTMENT);	

	}
	

	/**
	 * Webhook for the followup intent when the user has to confirm the appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.request.dayselection.hourselection.yes")
	public ActionResponse yesHourSelectionDaySelectionRequestAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> requestAppointmentIntentHandler.confirmAppointment(request),EVENT_REQUEST_APPOINTMENT);		
	}
	
	
	
	/**
	 * Webhook for querying the next appointment with the user's doctor
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.query")
	public ActionResponse queryAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> queryAppointmentIntentHandler.queryAppointment(request),EVENT_QUERY_APPOINTMENT);
	}

	/**
	 * Webhook for the followup intent when the user responds yes to request for a
	 * new appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.query - yes")
	public ActionResponse followupQueryAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> queryAppointmentIntentHandler.requestNewAppointment(request),EVENT_QUERY_APPOINTMENT);
	}

	/**
	 * Webhook for modifying an appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.modify")
	public ActionResponse modifyAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> modifyAppointmentIntentHandler.modifyAppointment(request),EVENT_MODIFY_APPOINTMENT);

	}

	/**
	 * Webhook for canceling the user's next appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.cancel")
	public ActionResponse cancelAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> cancelAppointmentIntentHandler.cancelAppointment(request),EVENT_CANCEL_APPOINTMENT);

	}

	/**
	 * Webhook for the followup intent when the user has decided to cancel the next
	 * appointment
	 * 
	 * @param request
	 * @return
	 */
	@ForIntent("appointment.cancel - yes")
	public ActionResponse followupCancelAppointmentIntent(ActionRequest request) {
		return this.applyPreconditions(request, e -> cancelAppointmentIntentHandler.confirmAppointmentCancelation(request),EVENT_CANCEL_APPOINTMENT);

	}

	}
