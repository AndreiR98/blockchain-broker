package uk.co.roteala.api.eth;

import lombok.Data;

@Data
public class EthRequestsWrapper {
    private EthRequest ethRequest;
    private EthRequestWithParam ethRequestWithParam;
}
