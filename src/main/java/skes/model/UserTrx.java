package skes.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Created by eyallevy on 09/02/18 .
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("USER-TRX")
public class UserTrx extends Message {

    User user;
    Trx trx;

    public UserTrx() {}

    public UserTrx(Trx trx, User user) {
        this.trx = trx;
        this.user = user;
    }

    @Override
    public String print() {
        return user.print() + " && " + trx.print() ;
    }

    @Override
    public String key() {
        return "{\"userId\":\"" + user.getUserId() + "\"}";
    }
}
