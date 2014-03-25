package net.respectnetwork.csp.application.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
 
public class DAOContextProvider implements ApplicationContextAware
{
        private static final Logger logger = LoggerFactory.getLogger(DAOContextProvider.class);

	private static ApplicationContext context = null;

	public static ApplicationContext getApplicationContext()
	{
		logger.info("getApplicationContext() " + context);
		return context;
	}

	public void setApplicationContext( ApplicationContext context ) throws BeansException
	{
		logger.info("setApplicationContext() " + context);
		this.context = context;
	}
}
