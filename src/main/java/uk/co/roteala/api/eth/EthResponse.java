package uk.co.roteala.api.eth;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EthResponse {
    private String id;
    private String jsonrpc;
    private String result;

    // Add constructor and getters
    // ...

    @Override
    public String toString() {
        return "EthResponse{" +
                "id=" + id +
                ", jsonrpc='" + jsonrpc + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}





