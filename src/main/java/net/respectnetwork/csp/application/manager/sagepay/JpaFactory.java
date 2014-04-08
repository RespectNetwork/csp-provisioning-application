package net.respectnetwork.csp.application.manager.sagepay;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sagepay.util.lang.ConfigProperties;


/**
 * Singleton to encapsulate creation of multiple JPA EntityManagerFactories for database
 * persistence. 
 * 
 * Clients should call:
 *  
 * <ul>
 * <li>createFactory(name, conf)</li>
 * <li>getFactory(name)</li>
 * <li>closeFactory(name)</li>
 * </ul>
 */
public class JpaFactory  {
	
	private static Logger LOG = LoggerFactory.getLogger(JpaFactory.class);
	private static JpaFactory INSTANCE = new JpaFactory();
	
	private Map<String,EntityManagerFactory> factories = 
		new HashMap<String,EntityManagerFactory>();

	/* singleton constructor */
	private JpaFactory() {}
	
	public static JpaFactory getInstance() {
		return INSTANCE;
	}

	public synchronized boolean isConnected(String persistenceUnit) {
		return this.factories.containsKey(persistenceUnit);
	}

	public synchronized JpaFactory createFactory(String persistenceUnit, ConfigProperties config) {
		final String[] prefixes = { "javax.persistence", "hibernate" };
		final Properties props = config.filterByPrefix(prefixes);
		return createFactory(persistenceUnit, props);
	}	
	
	public synchronized JpaFactory createFactory(String persistenceUnit, Properties props) {
		closeFactory(persistenceUnit);
		LOG.debug("createEntityManagerFactory() pu={} dburl={}", persistenceUnit,
				props.getProperty("javax.persistence.jdbc.url"));
		this.factories.put(persistenceUnit, 
				Persistence.createEntityManagerFactory(persistenceUnit, props));
		return this;
	}

	public synchronized EntityManagerFactory getFactory(String persistenceUnit) {
		return this.factories.get(persistenceUnit);
	}
	
	public synchronized void closeFactory(String persistenceUnit) {
		EntityManagerFactory factory = getFactory(persistenceUnit);
		if (factory != null && factory.isOpen()) { 
			factory.close();
		}
		this.factories.remove(persistenceUnit);
	}

}
