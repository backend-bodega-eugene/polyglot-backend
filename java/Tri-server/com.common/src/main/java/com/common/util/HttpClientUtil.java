package com.common.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class HttpClientUtil {
    @Resource
    private RestTemplate restTemplate;
    //请求头
    private Map<String, Object> headers = new HashMap<>();

    public void putHeader(String key, Object value) {
        headers.put(key, value);
    }

    private HttpHeaders getHeader() {
        HttpHeaders newHeader = new HttpHeaders();
        headers.entrySet().stream().forEach(header -> {
            newHeader.add(header.getKey(), header.getValue().toString());
        });
        return newHeader;
    }

    public Map<String, Object> getHeaders() {
        return CollectionUtils.isEmpty(headers) ? null : headers;
    }

    /**
     * 发送GET请求
     *
     * @param url
     * @param param
     * @return
     */
    public String doGet(String url, Map<String, String> param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return restTemplate.getForObject(url, String.class, param);
    }

    /**
     * post-json请求
     *
     * @param url
     * @param param
     * @return
     */
    public String doPostJson(String url, JSONObject param) {
        HttpHeaders headers = getHeader();
        if (CollectionUtils.isEmpty(headers.getOrEmpty(HttpHeaders.CONTENT_TYPE))) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        var entity = new HttpEntity(param, headers);
        return restTemplate.postForObject(url, entity, String.class);
    }

    /**
     * post表单请求
     *
     * @param url
     * @param param
     * @return
     */
    public String doPostForm(String url, Map<String, String> param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var entity = new HttpEntity(param, headers);
        return restTemplate.postForObject(url, entity, String.class);
    }
}