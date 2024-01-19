package uk.co.roteala.api.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.roteala.api.ResultStatus;
import uk.co.roteala.common.SignatureModel;
import uk.co.roteala.common.TransactionStatus;
import uk.co.roteala.common.monetary.Coin;
import uk.co.roteala.common.monetary.CoinConverter;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
    private String address;
    private String balance;
    private String virtualBalance;
    private Integer nonce;
    private List<String> transactionsIn;
    private List<String> transactionsOut;
    private String message;
    private ResultStatus result;
}
