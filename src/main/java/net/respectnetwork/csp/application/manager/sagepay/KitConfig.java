package net.respectnetwork.csp.application.manager.sagepay;

import java.io.FileNotFoundException;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sagepay.sdk.api.IApiConstants;
import com.sagepay.util.lang.ConfigProperties;
import com.sagepay.util.lang.UncheckedException;

/**
 * Configuration class to load configuration file for the demo applications.
 *   
 */
public class KitConfig  {
	
	private static Logger LOG = LoggerFactory.getLogger(KitConfig.class);
	
	public static ConfigProperties loadConfigProperties(final ServletContext context) {
		boolean loaded = false;
		ConfigProperties configProperties = new ConfigProperties(IApiConstants.VSP_ENV, IApiConstants.ALLOWED_ENVS);
		loaded = configProperties.addPropertiesFromClasspath(KitConstants.PROPS_FILE);
		if (loaded) {
			LOG.debug("Loaded base properties file '{}'", KitConstants.PROPS_FILE);
		}
		if (configProperties.getEnv() != null) {
			loaded |= mergeEnvironmentalProperties(configProperties);
		}
		if (context != null) {
			loaded |= mergeContextSpecificProperties(context, configProperties);
		}
		if (!loaded) {
			throw new UncheckedException(KitConstants.PROPS_FILE + " not found on classpath.");
		}
		return configProperties;
	}

	private static boolean mergeEnvironmentalProperties(ConfigProperties cp) {
		String envProps = cp.getEnvPropertiesFileName(KitConstants.PROPS_FILE);
		boolean loaded = cp.addPropertiesFromClasspath(envProps);
		if (loaded)
			LOG.debug("Loaded env specific properties file '{}'", envProps);
		return loaded;
	}

	private static boolean mergeContextSpecificProperties(final ServletContext context, ConfigProperties cp) {
		boolean overridden = false;
		String contextOverridesFile = context.getInitParameter(KitConstants.OVERRIDES_FILE_CONTEXT_PARAM_NAME);
		if (contextOverridesFile != null) {
			try {
				overridden = cp.addPropertiesFromFile(contextOverridesFile);
				if (overridden)
					LOG.debug("Loaded context specific properties file '{}'", contextOverridesFile);
			} catch (FileNotFoundException e) {
				LOG.error("File '{}' not found, properties NOT overridden", contextOverridesFile);
			}
		}
		return overridden;
	}
	
}
