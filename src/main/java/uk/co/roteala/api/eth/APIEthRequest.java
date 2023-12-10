package uk.co.roteala.api.eth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Slf4j
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class APIEthRequest {
    private String jsonrpc;
    private String id;
    private String method;
    private List<Object> params;

//    @JsonIgnore
//    public AbstractEthRequest getType() {
//        try {
//            Optional<Object> type = Optional.of(params.stream()
//                    .map(Object::getClass).findFirst());
//
//            log.info("Type: {}", type.get());
//
//            if(type.isEmpty()) {
//                throw new Exception();
//            }
//
//            if(type.get() instanceof String) {
//                List<String> strings = params.stream()
//                        .map(object -> Objects.toString(object, null)).collect(Collectors.toList());
//                return new EthRequest(jsonrpc, id, method, strings);
//            } else {
//                //return new EthRequestWithParam(jsonrpc, id, method, params);
//            }
//        }catch (Exception e) {
//            //
//        }
//        return null;
//    }
}
