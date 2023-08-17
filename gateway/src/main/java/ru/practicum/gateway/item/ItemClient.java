package ru.practicum.gateway.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import ru.practicum.gateway.client.BaseClient;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(long userId, ItemDto itemInnerDto) {
        return post("", userId, itemInnerDto);
    }

    public ResponseEntity<Object> getItemById(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> updateItem(long userId, long itemId, ItemDto itemInnerDto) {
        return patch("/" + itemId, userId, itemInnerDto);
    }

    public ResponseEntity<Object> getUserItems(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getItemsByText(long userId, String text) {
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get("/search?text={text}", userId, parameters);
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, IncomingCommentDto incomingCommentDto) {
        return post("/" + itemId + "/comment", userId, incomingCommentDto);
    }
}
