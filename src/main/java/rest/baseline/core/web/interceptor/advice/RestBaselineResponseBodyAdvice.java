package rest.baseline.core.web.interceptor.advice;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import rest.baseline.commom.model.ApiResponse;
import rest.baseline.commom.util.RestBaselineConstants;

/**
 * 
 * <code>RestBaselineResponseBodyAdvice</code> is used to set the metadata and
 * status info in the ApiResponse.<br>
 * 
 * This class implements the {@link ResponseBodyAdvice} and intercept all the
 * outgoing calls from the controller of the service which is actively using
 * RestBaseline library. If the return type of the controller function is of
 * type ApiResponse<T> or if its an unexpected error then it will intercept that
 * call and set the metadata like traceId, timestamp, API path and HTTP status
 * in the ApiResponse<T> or just traceId in case of unexpected error.
 * 
 * This feature can be disabled by using a specific property. That is
 * RestBaselineResponseBodyAdvice bean is created conditionally. If that
 * property is set to false then only this feature will be disabled. Otherwise
 * it will be enable by default.
 *
 * @param <Object>
 * 
 * @author Mindstix-ychawda, dchopra, sattar, susmitb
 */
@ControllerAdvice
@ConditionalOnProperty(name = RestBaselineConstants.REST_BASELINE_FEATURE_FLAG_NAME, havingValue = RestBaselineConstants.REST_BASELINE_FEATURE_FLAG_ENABLED_ON_VALUE, matchIfMissing = true)
public class RestBaselineResponseBodyAdvice<Object> implements ResponseBodyAdvice<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestBaselineResponseBodyAdvice.class);
    private static boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();
    private static Method errorMethod = null;

    @Autowired
    private Tracer tracer;

    static {
        try {
            errorMethod = BasicErrorController.class.getMethod("error", HttpServletRequest.class);
        } catch (NoSuchMethodException ex) {
            LOGGER.warn("Method not found error occurred while getting the errorMethod from HttpServletRequest", ex);
        } catch (SecurityException ex) {
            LOGGER.warn("Security error occurred while getting the errorMethod from HttpServletRequest", ex);
        }
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.getMethod().getReturnType().equals(ApiResponse.class) || returnType.getMethod().equals(errorMethod)) {
            if (DEBUG_ENABLED) {
                LOGGER.debug("return type of the respective controller is either ApiResponse<T> or its a error method.");
            }
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (DEBUG_ENABLED) {
            LOGGER.debug("Updating the reponse body with metadata and status details.");
        }

        /*
         * We are wrapping the logic inside this method in try block so that in
         * any case it fails then it should not affect the original API
         * response. In any case RestBaseline library should not affect the
         * service using this library.
         */
        try {
            if (body instanceof ApiResponse) {
                if (DEBUG_ENABLED) {
                    LOGGER.debug("response body is of type ApiResponse<T>, updating the metadata and status in the response body.");
                }
                HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
                Integer status = servletResponse.getStatus();
                ApiResponse apiResponse = (ApiResponse) body;
                getBasicDataFromResponse(request, apiResponse);
                apiResponse.setStatus(status);
                return (Object) apiResponse;
            } else {
                if (returnType.getMethod().equals(errorMethod)) {
                    if (DEBUG_ENABLED) {
                        LOGGER.debug("response body is of type errorMethod, updating the traceId in the error response body.");
                    }
                    Map<String, Object> respMap = (Map<String, Object>) body;
                    String traceId = getTraceId();
                    respMap.put(RestBaselineConstants.TRACE_ID_KEY, (Object) traceId);
                    return body;
                } else {
                    if (DEBUG_ENABLED) {
                        LOGGER.debug("response body is other than the expected return type. Returning the original body as it is.");
                    }
                    return body;
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Exception occured while populating the details in the ApiResponse in the RestBaseline library.", ex);
            return body;
        }
    }

    /**
     * This method is used to generate and set the traceId, timestamp and path
     * in the Api response.
     * 
     * @param request is an instance of {@link ServerHttpRequest} which provides
     *            the required data.
     * @param apiResponse {@link ApiResponse} object in which the data will be
     *            set.
     */
    private void getBasicDataFromResponse(ServerHttpRequest request, ApiResponse apiResponse) {
        String path = request.getURI().getPath();
        String traceId = getTraceId();
        apiResponse.setTraceId(traceId);
        apiResponse.setTimestamp(System.currentTimeMillis());
        apiResponse.setPath(path);
    }

    /**
     * This method is used to get the traceId from Tracer.
     * 
     * @return traceId in String format.
     */
    private String getTraceId() {
        String traceId = null;
        Span currentSpan = tracer.getCurrentSpan();
        if (null != currentSpan) {
            traceId = Span.idToHex(tracer.getCurrentSpan().getTraceId());
        } else {
            LOGGER.warn("TraceId is not available");
        }
        if (DEBUG_ENABLED) {
            LOGGER.debug("TraceId for the current request : {}", traceId);
        }
        return traceId;
    }
}
