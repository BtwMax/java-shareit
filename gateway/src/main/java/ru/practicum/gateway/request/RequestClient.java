package ru.practicum.gateway.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.gateway.client.BaseClient;

import java.util.Map;

@Service
public class RequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    @Autowired
    public RequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addRequest(long userId, IncomingItemRequestDto incomingItemRequestDto) {
        return post("", userId, incomingItemRequestDto);
    }

    public ResponseEntity<Object> getItemRequestById(long userId, long requestId) {
        return get("/" + requestId, userId);
    }

    public ResponseEntity<Object> getRequestorItemRequest(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllOtherItemRequests(long userId, Integer from, Integer size) {
        if (from == null || size == null) {
            return get("/all", userId);
        } else {
            Map<String, Object> parameters = Map.of(
                    "from", from,
                    "size", size
            );
            return get("/all?from={from}&size={size}", userId, parameters);
        }
    }
}
