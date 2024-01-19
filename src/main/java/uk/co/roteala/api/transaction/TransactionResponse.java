package uk.co.roteala.api.transaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.roteala.api.ResultStatus;
import uk.co.roteala.common.*;
import uk.co.roteala.common.monetary.Coin;
import uk.co.roteala.common.monetary.CoinConverter;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {
    private String hash;
    private Integer blockNumber;
    private String from;
    private String to;
    private Integer version;
    private Integer transactionIndex;
    private BigInteger amount;
    private BigInteger processingFees;
    private BigInteger networkFees;
    private String nonce;
    private long timeStamp;
    private long confirmations;
    private long blockTime;
    private String pubKeyHash;
    private TransactionStatus transactionStatus;
    private String message;
    private ResultStatus result;
}
