package com.mindstix.web.rest.baseline.core.annotation;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <code>RestBaselineConfiguration</code> is a configuration class which is
 * needed for rest baseline framework.<br>
 * 
 * This class contains the configuration required by the Rest baseline framework
 * to start and needs to be created by the client application. When client
 * application uses <code>RestBaseline</code> annotation, this configuration
 * will automatically get imported into the client application.
 * 
 * @author Mindstix-susmitb
 */
@Configuration
@ComponentScan(value = { "com.mindstix.web" })
public class RestBaselineConfiguration {

}
