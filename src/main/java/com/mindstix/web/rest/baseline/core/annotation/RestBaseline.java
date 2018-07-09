package com.mindstix.web.rest.baseline.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

/**
 * <code>RestBaseline</code> annotation used to configure the Rest baseline
 * framework in the client application.<br>
 * 
 * This annotation includes the <code>RestBaselineConfiguration</code> in the
 * client application.<br>
 * 
 * @author Mindstix-susmitb
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RestBaselineConfiguration.class)
public @interface RestBaseline {
    @AliasFor(annotation = Import.class, attribute = "value")
    Class<?>[] value() default { RestBaselineConfiguration.class };
}
