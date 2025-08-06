package com.app.manage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@Component
@ControllerAdvice
@Slf4j
public class DecodeRequestBodyAdvice implements RequestBodyAdvice {


    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter,
                                           Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        //获取请求头信息
        String body = null;
        try {
            // 从流中提取数据
            InputStream is = httpInputMessage.getBody();
            byte[] data = new byte[is.available()];
            is.read(data);
            body = new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DecodedHttpInputMessage(httpInputMessage.getHeaders(), new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage httpInputMessage, MethodParameter methodParameter,
                                Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage httpInputMessage, MethodParameter methodParameter,
                                  Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }

    static class DecodedHttpInputMessage implements HttpInputMessage {
        HttpHeaders headers;
        InputStream body;

        public DecodedHttpInputMessage(HttpHeaders headers, InputStream body) {
            this.headers = headers;
            this.body = body;
        }

        @Override
        public InputStream getBody() {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
