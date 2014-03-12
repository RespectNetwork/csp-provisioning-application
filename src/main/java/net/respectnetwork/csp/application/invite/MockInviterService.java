package net.respectnetwork.csp.application.invite;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import net.respectnetwork.csp.application.csp.CSPCloudName;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Memory Store of Inviter Codes
 *
 */
public class MockInviterService implements Inviter {
    
    
    /** Length of the Token Generated */
    private static int TOKEN_SIZE = 6;
    
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(MockInviterService.class);
    
    /** Memory Store of CloudName/InviterCode Pairs */
    private HashMap<CSPCloudName,String> inviterCodeStore = new HashMap<CSPCloudName, String>(1000);
    
    /** Location of Serialized File */
    private String inviteFileName;
    
    
    

    public String getInviteFileName() {
        return inviteFileName;
    }

    public void setInviteFileName(String inviteFileName) {
        this.inviteFileName = inviteFileName;
    }

    public MockInviterService(String inviteFileName) {
        super();
        

        try {
            this.inviteFileName = inviteFileName;
            FileInputStream fileIn = new FileInputStream(this.inviteFileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            logger.debug("Loading inviterCodeStore from  {} ...", inviteFileName);

            this.inviterCodeStore = (HashMap<CSPCloudName, String>) in
                    .readObject();

            in.close();
            fileIn.close();
        } catch (FileNotFoundException e) {
            logger.debug("No file found: {} : Assume it will be created as new.", this.inviteFileName);
        } catch (IOException e) {
            logger.debug("Problem Accessing File: {}", this.inviteFileName);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("Problen Serializing Invite MAP{}", e.getMessage());
        }
    }

    @Override
    public String createInviterCode(String csp, String cloudName) {
        
        CSPCloudName cn = new CSPCloudName(csp, cloudName.toLowerCase());
        String capInviterCode = inviterCodeStore.get(cn);
                
        if (capInviterCode == null) {
            capInviterCode = RandomStringUtils.randomAlphanumeric(TOKEN_SIZE).toUpperCase(); 
            inviterCodeStore.put(cn, capInviterCode);
            try{
                FileOutputStream fos = new FileOutputStream(this.inviteFileName);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(inviterCodeStore);
                oos.close();
            }  catch (FileNotFoundException e) {
                e.printStackTrace();
                logger.warn("Problem Persisting InviteCode: {} : {} :{} : {} ", csp, cloudName, capInviterCode, e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("Problem Persisting InviteCode: {} :{} :{} : {} ", csp, cloudName, capInviterCode, e.getMessage());
            } 
                
        }
        
        return capInviterCode;
    }

    @Override
    public String getCloudNameFromInviterCode(String inviterCode, String csp) {
        
        String match = null;

        for (@SuppressWarnings("rawtypes") Map.Entry entry : inviterCodeStore.entrySet()) {
            logger.debug("Key/Value: {} -> {}", entry.getKey(), entry.getValue() );
            String storedInviter = (String)entry.getValue();
            String storedCSP = ((CSPCloudName)(entry.getKey())).getCsp();
            if (storedInviter.equalsIgnoreCase(inviterCode) && storedCSP.equalsIgnoreCase(csp)){
                logger.debug("Match for {} = {}",inviterCode ,entry.getKey() );
                match = ((CSPCloudName)(entry.getKey())).getCloudName();
            }
        }      
        return match;
    }
    
    
 

}
