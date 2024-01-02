package uk.co.roteala.api.eth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import uk.co.roteala.common.*;
import uk.co.roteala.common.events.PeersContainer;
import uk.co.roteala.common.monetary.Vault;

import java.util.List;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "method",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EthRequest.class, name = "eth_getBalance"),
        @JsonSubTypes.Type(value = EthRequest.class, name = "eth_chainId"),
        @JsonSubTypes.Type(value = EthRequest.class, name = "net_version"),
        @JsonSubTypes.Type(value = EthRequest.class, name = "eth_blockNumber"),
        @JsonSubTypes.Type(value = EthRequest.class, name = "eth_gasPrice"),
        @JsonSubTypes.Type(value = EthRequest.class, name = "eth_getTransactionCount"),
        @JsonSubTypes.Type(value = EthRequest.class, name = "eth_sendRawTransaction"),
        @JsonSubTypes.Type(value = EthRequest.class, name = "send_transaction"),
        @JsonSubTypes.Type(value = EthRequestWithMultiTypeParams.class, name = "eth_getBlockByNumber"),
        @JsonSubTypes.Type(value = EthRequestWithParam.class, name = "eth_estimateGas")
})
public abstract class AbstractEthRequest<T> {
    private String jsonrpc;
    private String id;
    private String method;
    protected List<T> params;
}
