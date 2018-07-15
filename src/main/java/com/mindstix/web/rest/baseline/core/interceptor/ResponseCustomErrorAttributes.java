package com.mindstix.web.rest.baseline.core.interceptor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

import com.mindstix.web.rest.baseline.common.util.RestBaselineConstants;

/**
 * <code>ResponseCustomErrorAttributes</code> intercepter class handles error
 * attributes in the response in case of error.<br>
 * 
 * Here framework is removing the <code>exception</code> attribute from the
 * error response. This attribute is removed to avoid exposing the internal
 * exception and error handling to outside world but it is logged for the
 * developers to get the error details.<br>
 * 
 * @author Mindstix-susmitb
 */
@Component
@ConditionalOnProperty(name = RestBaselineConstants.REST_BASELINE_FEATURE_FLAG_NAME, havingValue = RestBaselineConstants.REST_BASELINE_FEATURE_FLAG_ENABLED_ON_VALUE, matchIfMissing = true)
public class ResponseCustomErrorAttributes extends DefaultErrorAttributes {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseCustomErrorAttributes.class);

    private static boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();

    @Override
    public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, includeStackTrace);
        if (errorAttributes.containsKey(RestBaselineConstants.ERROR_RESPONSE_EXCEPTION_KEY)) {
            if (DEBUG_ENABLED)
                LOGGER.debug("Removing the object : {} with value : {} from error response", RestBaselineConstants.ERROR_RESPONSE_EXCEPTION_KEY,
                        errorAttributes.get(RestBaselineConstants.ERROR_RESPONSE_EXCEPTION_KEY));
            errorAttributes.remove(RestBaselineConstants.ERROR_RESPONSE_EXCEPTION_KEY);
        }
        return errorAttributes;
    }
}
