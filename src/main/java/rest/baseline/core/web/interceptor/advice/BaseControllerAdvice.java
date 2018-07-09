package rest.baseline.core.web.interceptor.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import rest.baseline.commom.model.ApiResponse;
import rest.baseline.commom.util.RestBaselineConstants;

/**
 * <code>BaseControllerAdvice</code> is a base controller advice.<br>
 * 
 * This controller advice is used to handle the Spring's default API errors and
 * unhandled error from the service which is using this framework.<br>
 * 
 * @author Mindstix-susmitb
 */
@ControllerAdvice
@ConditionalOnProperty(name = RestBaselineConstants.REST_BASELINE_FEATURE_FLAG_NAME, havingValue = RestBaselineConstants.REST_BASELINE_FEATURE_FLAG_ENABLED_ON_VALUE, matchIfMissing = true)
public class BaseControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseControllerAdvice.class);

    /**
     * This method is used to handle the Spring's default error.
     * 
     * @param ex The {@link Exception} object.
     * @return {@link ApiResponse} but it will always throws the exception as it
     *         is.
     * @throws Exception
     */
    @ExceptionHandler(value = { HttpRequestMethodNotSupportedException.class, MethodArgumentTypeMismatchException.class,
                                MissingServletRequestParameterException.class })
    @ResponseBody
    public ApiResponse<Object> handleSpringException(Exception ex) throws Exception {
        throw ex;
    }

    /**
     * This method is used to handle all the unhandled exception from the
     * service which is using this framework.
     * 
     * @param ex The {@link Exception} object.
     * @return The {@link ApiResponse} object where based on the exception.
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleBaseException(Exception ex) {
        LOGGER.warn("Exception occured while processing the request", ex);
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        LOGGER.info("exception : {}", ex.getClass().getCanonicalName());
        apiResponse.setError(RestBaselineConstants.DEFAULT_ERROR_CODE_FOR_UNEXPECTED_ERROR);
        apiResponse.setMessage(RestBaselineConstants.DEFAULT_ERROR_MESSAGE_FOR_UNEXPECTED_ERROR);
        return apiResponse;
    }
}
