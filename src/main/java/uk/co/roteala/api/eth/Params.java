package uk.co.roteala.api.eth;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Params {
    private String to;
    private String from;
    private String gas;
    private String gasPrice;
    private String value;
    private String data;
}
