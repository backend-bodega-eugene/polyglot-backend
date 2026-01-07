package com.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {


    private String apiKey="";

    private final RestTemplate rest = new RestTemplate();

    public String ask(String content) {
        try {
            String url = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-4o-mini");
            body.put("temperature", 0.6);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "你是群聊里的AI成员，说话简短自然，不要长篇大论。"));
            messages.add(Map.of("role", "user", "content", content));

            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> res = rest.postForEntity(url, entity, Map.class);

            Map choice = (Map) ((List) res.getBody().get("choices")).get(0);
            Map message = (Map) choice.get("message");

            return message.get("content").toString();
        } catch (Exception e) {
           // throw new RuntimeException(e);
        return "在中国我连不上服务器!!!";
        }
    }
}
