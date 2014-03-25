package net.respectnetwork.csp.application.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DAOFactory
{
        private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);

	private static DAOFactory singleton = null;

	private CSPDAO	            cSPDAO;
	private DependentCloudDAO   dependentCloudDAO;

	public static DAOFactory getInstance()
	{
		if( singleton != null )
		{
			return singleton;
		}
		synchronized( logger )
		{
			logger.info("Get DAOFactory singleton");
			if( singleton == null )
			{
//				ApplicationContext context = null;
// 				context = new ClassPathXmlApplicationContext("classpath*:spring.xml");
// 				context = new ClassPathXmlApplicationContext("/u01/xdi/csp-provisioning-application/src/main/webapp/WEB-INF/spring/CSPApp/spring.xml");
				ApplicationContext context = DAOContextProvider.getApplicationContext();
				singleton = (DAOFactory) context.getBean("daoFactory");
				if( singleton == null )
				{
					logger.error("Get DAOFactory singleton failed");
				}
				else
				{
					logger.info("Get DAOFactory singleton allocated");
				}
			}
			else
			{
				logger.info("Get DAOFactory singleton ok");
			}
		}
		return singleton;
	}

	public DAOFactory()
	{
		this.cSPDAO = null;
		this.dependentCloudDAO = null;
	}

	public CSPDAO getCSPDAO()
	{
		return this.cSPDAO;
	}

	public void setCSPDAO( CSPDAO cSPDAO )
	{
		this.cSPDAO = cSPDAO;
	}

	public DependentCloudDAO getDependentCloudDAO()
	{
		return this.dependentCloudDAO;
	}

	public void setDependentCloudDAO( DependentCloudDAO dependentCloudDAO )
	{
		this.dependentCloudDAO = dependentCloudDAO;
	}
}
