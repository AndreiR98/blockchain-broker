package uk.co.roteala.api.eth;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;
@EqualsAndHashCode(callSuper = true)
public class EthRequest extends AbstractEthRequest<String> {
    @Override
    public void setParams(List<String> params) {
        super.setParams(params);
    }
}
