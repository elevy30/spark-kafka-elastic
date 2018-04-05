package skes.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by eyallevy on 16/02/18 .
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "messageType", defaultImpl = User.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = User.class, name = "USER"),
        @JsonSubTypes.Type(value = Trx.class,  name = "TRX"),
        @JsonSubTypes.Type(value = UserTrx.class,  name = "USER-TRX")
})
public abstract  class Message {

    @JsonIgnore
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (IOException  e) {
            e.printStackTrace();
            return null;
        }
    }

    public abstract String key();
    public abstract String print();
}
