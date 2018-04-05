package skes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eyallevy on 09/02/18 .
 */
@Data
public class Window {

    @JsonIgnore
    static Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Date start;
    public Date end;

    @Override
    public String toString() {
        return "Window {" + "start=" + formatter.format(start) + ", end=" + formatter.format(end) + '}';
    }
}
