package es.ja.csalud.sas.botcitas.botmanager.intents;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class AgentResponses {
	private static final String BUNDLE_NAME =  "messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private AgentResponses() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
