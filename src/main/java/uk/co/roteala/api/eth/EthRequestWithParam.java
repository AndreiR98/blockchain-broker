package uk.co.roteala.api.eth;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;


public class EthRequestWithParam extends AbstractEthRequest<Params> {
    @Override
    public void setParams(List<Params> params) {
        super.setParams(params);
    }
}
