package skes.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Created by eyallevy on 09/02/18 .
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TRX")
public class Trx extends  Message{

    public Window window;
    public int userId;
    public int sum;
    public int count;
    public int trxId;

    public Trx(){}

    @Override
    public String print() {
        return "Trx {" + window + ", userId=" + userId + ", sum=" + sum + ", count=" + count + ", trxId=" + trxId + '}';
    }

    @Override
    public String key() {
        return "{\"userId\":\"" + userId + "\"}";
    }
}
