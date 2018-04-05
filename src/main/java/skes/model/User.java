package skes.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Created by eyallevy on 09/02/18 .
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("USER")
public class User extends Message {

    String userId;

    public User(){}

    public User(String userId){
        this.userId = userId;
    }

    @Override
    public String print() {
        return "User {userId=" + userId + '}';
    }

    @Override
    public String key() {
        return "{\"userId\":\"" + userId + "\"}";
    }
}
