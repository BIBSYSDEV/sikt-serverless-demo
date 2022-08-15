package no.unit.nva.bergen;

import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.commons.json.JsonUtils;

public class Message implements JsonSerializable {
    
    public static final String TEXT_FIELD = "text";
    @JsonProperty(TEXT_FIELD)
    private final String text;
    
    @JsonCreator
    public Message(@JsonProperty(TEXT_FIELD) String text) {
        this.text = text;
    }
    
    public static Message fromJson(String messageString) {
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(messageString, Message.class)).orElseThrow();
    }
    
    public String getText() {
        return text;
    }
    
    @Override
    public String toString() {
        return toJsonString();
    }
}
