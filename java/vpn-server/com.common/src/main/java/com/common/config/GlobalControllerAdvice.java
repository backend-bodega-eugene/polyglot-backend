package com.common.config;

import com.common.annotation.DirectReturn;
import com.common.exception.ApplicationException;
import com.common.exception.BizException;
import com.common.result.PageResult;
import com.common.result.Result;
import com.common.result.ResultEnum;
import com.common.rpc.RPCResult;
import com.common.util.Money;
import com.common.util.StringUtils;
import com.common.web.editor.CustomMoneyEditor;
import com.common.web.editor.LocalDateEditor;
import com.common.web.editor.LocalDateTimeEditor;
import com.common.web.editor.StringEditor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: cookie
 * @Date: 2018/7/26 15:09
 * @Description: 全局捕获异常和自定义全局捕获异常
 */
@Slf4j
@ControllerAdvice(annotations = RestController.class)
public class GlobalControllerAdvice implements ResponseBodyAdvice {

    @Autowired(required = false)
    private HttpServletRequest request;

    //404
    @ResponseBody
    @ExceptionHandler
    public Result handleResourceNotFoundException(NoHandlerFoundException error) {
        log.error("handleResourceNotFoundException:url->{}", request.getRequestURI());
        request.setAttribute("exception", true);
        return new Result(ResultEnum.RESOURCE_NOT_FOUND.getCode(), error.getMessage());
    }

    //validateException
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result validationErrorHandler(MethodArgumentNotValidException error) {
        log.error("MethodArgumentNotValidException:url->{}", request.getRequestURI());
        List<String> errors = error.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
        request.setAttribute("exception", true);
        return new Result(HttpStatus.BAD_REQUEST.value(), errors.get(0));
    }

    //validateException
    @ResponseBody
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result bizExceptionHandler(ConstraintViolationException e) {
        log.error("ConstraintViolationException:url->{}", request.getRequestURI());
        List<String> msgList = new ArrayList<>();
        for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
            Path propertyPath = constraintViolation.getPropertyPath();
            String name = null;
            if (propertyPath != null) {
                String pathStr = propertyPath.toString();
                int index = pathStr.indexOf(".");
                if (index != -1) {
                    name = pathStr.substring(index + 1);
                }
            }
            msgList.add("参数->" + (StringUtils.isNotBlank(name) ? name : "") + constraintViolation.getMessage());
        }
        String messages = msgList.stream().collect(Collectors.joining(";"));

        request.setAttribute("exception", true);
        return new Result(HttpStatus.BAD_REQUEST.value(), messages);
    }

    //ApplicationException
    @ResponseBody
    @ExceptionHandler(value = ApplicationException.class)
    public Result ApplicationExceptionHandler(ApplicationException e) {
        log.error("ApplicationException:url->{}", request.getRequestURI());
        log.error("ApplicationException json error->code:" + " msg:" + e.getMessage());
        request.setAttribute("exception", true);
        return new Result(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    //bizException
    @ResponseBody
    @ExceptionHandler(value = BizException.class)
    public Result bizExceptionHandler(BizException e) {
        log.error("BizException:url->{}", request.getRequestURI());
        log.error("execute json error->code:" + e.getCode() + " msg:" + e.getMessage());
        request.setAttribute("exception", true);
        return new Result(e.getCode(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result exceptionHandler(Exception ex) {
        request.setAttribute("exception", true);
        if (ex instanceof DuplicateKeyException) {
            log.error("数据重复", ex);
            return new Result(2, "数据已存在");
        }
        log.error("业务执行失败", ex);
        return new Result(-1, "业务执行失败");
    }


    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        boolean isSpringfox = methodParameter.getDeclaringClass().getName().contains("springfox");
        boolean isDirectReturn = methodParameter.getMethod().getAnnotation(DirectReturn.class) != null;
        return !isSpringfox && !isDirectReturn;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (body == null) {
            //return aClass == StringHttpMessageConverter.class ? JSON.toJSONString(new Result()) : new Result();
            return new Result("");
        } else if (body instanceof Page) {
            var page = PageResult.convert((Page) body);
            return new Result<>(page);
        } else if (body instanceof Result || body instanceof RPCResult) {
            return body;
        }
        return new Result(body);
//        else {
////            if(true==methodParameter.getMethod().getName().equals("LineQuery")){
////                List<VpnLineResponseParam> lst=(List<VpnLineResponseParam>)body;
////                for (VpnLineResponseParam temp : lst) {
////                    for (VpnLine line: temp.getNationVpnLine()){
////                        line.setVmess(AES.LineAESDncrypt(line.getVmess()));
////                    }
////                }
//////               String bodyStr = JSON.toJSONString(body);
//////               String encodeStr=  AES.LineAESEncrypt(bodyStr);
////////               bodyStr=AES.LineAESDncrypt(encodeStr);
////////               System.out.println(bodyStr);
////             //   body=encodeStr;
////            }
//
//        }

    }


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDateTime.class, new LocalDateTimeEditor(true));
        binder.registerCustomEditor(LocalDate.class, new LocalDateEditor(true));
        binder.registerCustomEditor(String.class, new StringEditor());
        binder.registerCustomEditor(Money.class, new CustomMoneyEditor());
    }
}
