package net.respectnetwork.csp.application.manager;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class RegisterDependentCloudThread implements Runnable
{

   /** Class Logger */
   private static final Logger logger = LoggerFactory
           .getLogger(RegisterDependentCloudThread.class);
   
   private CloudName dependentCloudName = null;
   private CloudNumber depCloudNumber = null;
   private CSP                                  cspRegistrar                         = null;
   private String                               rcBaseURL                            = "";
   private String dependentToken = "";
   private String s_dependentBirthDate = "";
   private CloudName guardianCloudName = null;
   private String guardianToken = "";
   
   @Override
   public void run()
   {
      CloudNumber depCloudNumber = CloudNumber.createRandom(dependentCloudName.getCs());
      RegisterUserThread rut = new RegisterUserThread();
      rut.setCloudName(dependentCloudName);
      rut.setCloudNumber(depCloudNumber);
      rut.setCspRegistrar(cspRegistrar);
      rut.setRcBaseURL(rcBaseURL);
      rut.setUserPassword(dependentToken);
      rut.setVerifiedEmail(" ");
      rut.setVerifiedPhone(" ");
      rut.setCdc(RegistrationManager.depCloudNameDiscountCode);
      Thread t = new Thread(rut);
      t.start();
      try
       {
          t.join();
       } catch (InterruptedException e2)
       {
          // TODO Auto-generated catch block
          e2.printStackTrace();
       }
         
       
    
    // Common Data

    CloudNumber guardianCloudNumber = null;
      CloudNumber dependentCloudNumber = null;
      PrivateKey guardianPrivateKey = null;
      PrivateKey dependentPrivateKey = null;
    boolean withConsent = true;
    
    Date dependentBirthDate = null;
    
    BasicCSPInformation cspInformation = (BasicCSPInformation)cspRegistrar.getCspInformation();
    
    // Resolve Cloud Numbers from Name
    
      XDIDiscoveryClient discovery = cspInformation.getXdiDiscoveryClient();

      try {
          SimpleDateFormat format = 
                  new SimpleDateFormat("MM/dd/yyyy");
          dependentBirthDate = format.parse(s_dependentBirthDate);
      } catch (ParseException e) {
           logger.debug("Invalid Dependent BirthDate.");
           
      }

    for (int tries = 0 ; tries < 5 ; tries++) {

       try {
          logger.debug("Waiting for five seconds to allow for the newly registered dependent name in discovery");
          Thread.sleep(5000);
       } catch (InterruptedException e1) {
          
       }
      
         try {
             
            XDIDiscoveryResult guardianRegistry = discovery.discoverFromRegistry(
                     XDI3Segment.create(guardianCloudName.toString()), null);
             
             XDIDiscoveryResult dependentRegistry = discovery.discoverFromRegistry(
                     XDI3Segment.create(dependentCloudName.toString()), null);
             
             guardianCloudNumber = guardianRegistry.getCloudNumber();
             dependentCloudNumber = dependentRegistry.getCloudNumber();
             
             String guardianXdiEndpoint = guardianRegistry.getXdiEndpointUri();
             String dependentXdiEndpoint = dependentRegistry.getXdiEndpointUri();

             guardianPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(guardianCloudNumber, guardianXdiEndpoint, guardianToken);
             logger.debug("GuardianPrivateKey Algo: " + guardianPrivateKey.getAlgorithm());

             dependentPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(dependentCloudNumber, dependentXdiEndpoint, dependentToken);
             logger.debug("DependentPrivateKey Algo: " + dependentPrivateKey.getAlgorithm());
             
             if (guardianCloudNumber == null || dependentCloudNumber == null) {
                 logger.debug("Un-registered Cloud Name.");
                 continue;
             }
             break;

         } catch (Xdi2ClientException e) {
             logger.debug("Problem with Cloud Name Provided.");
             e.printStackTrace();
             logger.debug(e.getMessage());
             continue;
         } catch (GeneralSecurityException gse){
          logger.debug("Problem retrieving signatures.");
          gse.printStackTrace();
          logger.debug(gse.getMessage());
             continue;
         }
         
    }
    if(guardianCloudNumber != null && dependentCloudNumber != null) {
         try {
             // Set User Cloud Data
          cspRegistrar.setGuardianshipInCloud(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianToken, guardianPrivateKey);
          
          // Set CSP Cloud Data
          cspRegistrar.setGuardianshipInCSP(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
           
           // Set MemberGraph Data
          cspRegistrar.setGuardianshipInRN(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
          
          
           
         } catch (Xdi2ClientException e) {
          logger.debug("Xdi2ClientException: " + e.getMessage());
             e.printStackTrace();
         }
    }


   }

   public CloudName getDependentCloudName()
   {
      return dependentCloudName;
   }

   public void setDependentCloudName(CloudName dependentCloudName)
   {
      this.dependentCloudName = dependentCloudName;
   }

   public CloudNumber getDepCloudNumber()
   {
      return depCloudNumber;
   }

   public void setDepCloudNumber(CloudNumber depCloudNumber)
   {
      this.depCloudNumber = depCloudNumber;
   }

   public CSP getCspRegistrar()
   {
      return cspRegistrar;
   }

   public void setCspRegistrar(CSP cspRegistrar)
   {
      this.cspRegistrar = cspRegistrar;
   }

   public String getRcBaseURL()
   {
      return rcBaseURL;
   }

   public void setRcBaseURL(String rcBaseURL)
   {
      this.rcBaseURL = rcBaseURL;
   }

   public String getDependentToken()
   {
      return dependentToken;
   }

   public void setDependentToken(String dependentToken)
   {
      this.dependentToken = dependentToken;
   }

   public String getS_dependentBirthDate()
   {
      return s_dependentBirthDate;
   }

   public void setS_dependentBirthDate(String s_dependentBirthDate)
   {
      this.s_dependentBirthDate = s_dependentBirthDate;
   }

   public CloudName getGuardianCloudName()
   {
      return guardianCloudName;
   }

   public void setGuardianCloudName(CloudName guardianCloudName)
   {
      this.guardianCloudName = guardianCloudName;
   }

   public String getGuardianToken()
   {
      return guardianToken;
   }

   public void setGuardianToken(String guardianToken)
   {
      this.guardianToken = guardianToken;
   }

}
