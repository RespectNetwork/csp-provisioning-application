package net.respectnetwork.csp.application.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DAOFactory
{
        private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);

	private static DAOFactory singleton = null;

	private CSPDAO	              cSPDAO;
	private PaymentDAO            paymentDAO;
	private InviteDAO             inviteDAO;
	private InviteResponseDAO     inviteResponseDAO;
	private GiftCodeDAO           giftCodeDAO;
	private GiftCodeRedemptionDAO giftCodeRedemptionDAO;
	private DependentCloudDAO     dependentCloudDAO;
	private SignupInfoDAO         signupInfoDAO;

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
		this.paymentDAO = null;
		this.inviteDAO = null;
		this.inviteResponseDAO = null;
		this.giftCodeDAO = null;
		this.giftCodeRedemptionDAO = null;
		this.dependentCloudDAO = null;
		this.signupInfoDAO = null;
	}

	public CSPDAO getCSPDAO()
	{
		return this.cSPDAO;
	}

	public void setCSPDAO( CSPDAO cSPDAO )
	{
		this.cSPDAO = cSPDAO;
	}

	public PaymentDAO getPaymentDAO()
	{
		return this.paymentDAO;
	}

	public void setPaymentDAO( PaymentDAO paymentDAO )
	{
		this.paymentDAO = paymentDAO;
	}

	public InviteDAO getInviteDAO()
	{
		return this.inviteDAO;
	}

	public void setInviteDAO( InviteDAO inviteDAO )
	{
		this.inviteDAO = inviteDAO;
	}

	public InviteResponseDAO getInviteResponseDAO()
	{
		return this.inviteResponseDAO;
	}

	public void setInviteResponseDAO( InviteResponseDAO inviteResponseDAO )
	{
		this.inviteResponseDAO = inviteResponseDAO;
	}

	public GiftCodeDAO getGiftCodeDAO()
	{
		return this.giftCodeDAO;
	}

	public void setGiftCodeDAO( GiftCodeDAO giftCodeDAO )
	{
		this.giftCodeDAO = giftCodeDAO;
	}

	public GiftCodeRedemptionDAO getGiftCodeRedemptionDAO()
	{
		return this.giftCodeRedemptionDAO;
	}

	public void setGiftCodeRedemptionDAO( GiftCodeRedemptionDAO giftCodeRedemptionDAO )
	{
		this.giftCodeRedemptionDAO = giftCodeRedemptionDAO;
	}

	public DependentCloudDAO getDependentCloudDAO()
	{
		return this.dependentCloudDAO;
	}

	public void setDependentCloudDAO( DependentCloudDAO dependentCloudDAO )
	{
		this.dependentCloudDAO = dependentCloudDAO;
	}

   public SignupInfoDAO getSignupInfoDAO()
   {
      return signupInfoDAO;
   }

   public void setSignupInfoDAO(SignupInfoDAO signupInfoDAO)
   {
      this.signupInfoDAO = signupInfoDAO;
   }
}
