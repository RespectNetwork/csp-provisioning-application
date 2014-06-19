Respect Network CSP Reference Implementation
============================

### Dependencies

XDI2-Client: https://github.com/projectdanube/xdi2/tree/master/client

        <xdi2-version>0.4.2</xdi2-version>

SDK-CSP: https://github.com/RespectNetwork/sdk-csp/tree/master

        <cspsdk-version>0.3.3</cspsdk-version>


### How to  Build
To  build just run

    mvn clean install

### How to run

    mvn jetty:run

Then the Reference implementation is available at

        http://localhost:7073/csp-provisioning-application

First  Create an Invitation using the default CSP at

        http://localhost:7072/csp-provisioning-application/createInvitation/testCSP

This will allow you to  start the registration process

### Configuration Management.

Various configurable parameters for this service are located in property files in
./src/main/resources

To  customize your CSP's configuration use the following config. mamnagement pattern.

 1. By default csp.default.properties is applied.

 2. Setting the -Dcsp.env System Property e.g. -Dcsp.env=dev will use the registration.${csp.env}.properties. Valid entries are dev, stage or ote.

 3. You can also  specify  a property file outside of the WAR using -Dcspprop.location=path_to_prop_file. The csp.properties file in this directory will be used. This is intended for use in production where  we do  not want sensitive information in github.

#### Application Context ####

    <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="locations">
            <list>
                <value>classpath:csp.default.properties</value>
                <value>classpath:csp.${csp.env}.properties</value>
                <value>file:${cspprop.location}/csp.properties</value>
            </list>
        </property>
        <property name="ignoreResourceNotFound" value="true"/>
        <property name="ignoreUnresolvablePlaceholders" value="false" />
    </bean>

We also use property files for 

 1. Mail: ./main/resources/mail.properties
 2. Twilio: ./main/resources/twilio.properties
 3. Simple Notification Templates (Mail and SMS): ./main/resources/notification.properties
