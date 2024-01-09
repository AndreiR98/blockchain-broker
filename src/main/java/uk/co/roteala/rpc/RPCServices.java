package uk.co.roteala.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;
import uk.co.roteala.api.eth.AbstractEthRequest;
import uk.co.roteala.api.eth.EthRequest;
import uk.co.roteala.api.eth.EthRequestWithParam;
import uk.co.roteala.api.eth.EthResponse;
import uk.co.roteala.common.Account;
import uk.co.roteala.common.ChainState;
import uk.co.roteala.common.MempoolTransaction;
import uk.co.roteala.common.storage.ColumnFamilyTypes;
import uk.co.roteala.common.storage.Operator;
import uk.co.roteala.common.storage.SearchField;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.configs.BrokerConfigs;
import uk.co.roteala.core.Blockchain;
import uk.co.roteala.core.rlp.Numeric;
import uk.co.roteala.exceptions.RPCException;
import uk.co.roteala.exceptions.errorcodes.RPCErrorCode;
import uk.co.roteala.security.ECKey;
import uk.co.roteala.security.utils.CryptographyUtils;
import uk.co.roteala.security.utils.HashingService;
import uk.co.roteala.storage.Storages;
import uk.co.roteala.utils.BlockchainUtils;
import uk.co.roteala.utils.Constants;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RPCServices {
    private final BrokerConfigs brokerConfigs;
    private final Storages storages;
    private final Sinks.Many<MempoolTransaction> mempoolSink;
    public EthResponse processRequest(AbstractEthRequest abstractEthRequest) {
        System.out.println(abstractEthRequest.getMethod());
        switch (abstractEthRequest.getMethod()) {
            case "eth_chainId":
                return new EthResponse(abstractEthRequest.getId(), abstractEthRequest.getJsonrpc(),
                        BlockchainUtils.convertToETHHex(brokerConfigs.getChainId()));
            case "net_version":
                return new EthResponse(abstractEthRequest.getId(), abstractEthRequest.getJsonrpc(),
                        Constants.DEFAULT_NETWORK_VERSION);
            case "eth_getBalance":
                return getBalance(abstractEthRequest);
            case "eth_blockNumber":
                return blockNumber(abstractEthRequest);
            case "eth_gasPrice":
                return gasPrice(abstractEthRequest);
            case "eth_estimateGas":
                return gasEstimate(abstractEthRequest);
            case "eth_getTransactionCount":
                return transactionCount(abstractEthRequest);
            case "eth_sendRawTransaction":
                return sendRawTransaction(abstractEthRequest);
            case "send_transaction":
                return processSendTransaction(abstractEthRequest);
            default:
                return new EthResponse(abstractEthRequest.getId(), abstractEthRequest.getJsonrpc(), "Method not supported");
        }
    }

    private EthResponse getBalance(AbstractEthRequest abstractEthRequest) {
        EthResponse response = new EthResponse();
        response.setId(abstractEthRequest.getId());
        response.setJsonrpc(abstractEthRequest.getJsonrpc());

        try {
            EthRequest ethRequest = (EthRequest) abstractEthRequest;

            final Optional<String> accountAddressOptional = ethRequest.getParams()
                    .stream().findFirst();

            if(accountAddressOptional.isEmpty()) {
                throw new RPCException(RPCErrorCode.MALFORMED_PAYLOAD);
            }

            final String accountAddress = accountAddressOptional.get().toLowerCase();

            Account account = Optional.ofNullable((Account) storages.getStorage(StorageTypes.STATE)
                            .get(ColumnFamilyTypes.ACCOUNTS, accountAddress.getBytes(StandardCharsets.UTF_8)))
                    .orElseGet(() -> Account.empty(accountAddress));

            final String accountBalance = "0x"+(account.getBalance().add(account.getVirtualBalance())
                    .toString(16));

            response.setResult(accountBalance);
        } catch (Exception e) {
            log.info("Error:{}", e);
            response.setResult("ERROR!");
        }
        return response;
    }

    private EthResponse blockNumber(AbstractEthRequest abstractEthRequest) {
        EthResponse response = new EthResponse();
        response.setId(abstractEthRequest.getId());
        response.setJsonrpc(abstractEthRequest.getJsonrpc());

        try {
            EthRequest ethRequest = (EthRequest) abstractEthRequest;

            if(ethRequest.getParams().isEmpty()) {
                final String lastBlockIndex = BlockchainUtils
                        .convertToETHHex(((ChainState) storages.getStorage(StorageTypes.STATE)
                        .get(ColumnFamilyTypes.STATE, Constants.DEFAULT_STATE_NAME.getBytes(StandardCharsets.UTF_8))
                        ).getLastBlockIndex());
                response.setResult(lastBlockIndex);
            }
        } catch (Exception e) {
            response.setResult("ERROR!");
        }
        return response;
    }

    private EthResponse gasPrice(AbstractEthRequest abstractEthRequest) {
        EthResponse response = new EthResponse();
        response.setId(abstractEthRequest.getId());
        response.setJsonrpc(abstractEthRequest.getJsonrpc());

        try {
//            final String gasPrice = "0x"+((ChainState) storages.getStorage(StorageTypes.STATE)
//                            .get(ColumnFamilyTypes.STATE, Constants.DEFAULT_STATE_NAME
//                                    .getBytes(StandardCharsets.UTF_8)))
//                    .getNetworkFees().toString(16);

            final String gasPrice = "0x"+((ChainState) storages.getStorage(StorageTypes.STATE)
                    .get(ColumnFamilyTypes.STATE, Constants.DEFAULT_STATE_NAME.getBytes(StandardCharsets.UTF_8)))
                    .getNetworkFees().toString(16);
            response.setResult(gasPrice);
        } catch (Exception e) {
            response.setResult("ERROR!");
        }
        return response;
    }

    private EthResponse gasEstimate(AbstractEthRequest abstractEthRequest) {
        EthResponse response = new EthResponse();
        response.setId(abstractEthRequest.getId());
        response.setJsonrpc(abstractEthRequest.getJsonrpc());

        try {
            EthRequestWithParam ethRequest = (EthRequestWithParam) abstractEthRequest;

            final String estimatedGas = "0x5208";

            response.setResult(estimatedGas);
        } catch (Exception e) {
            response.setResult("ERROR!");
        }
        return response;
    }

    private EthResponse transactionCount(AbstractEthRequest abstractEthRequest) {
        EthResponse response = new EthResponse();
        response.setId(abstractEthRequest.getId());
        response.setJsonrpc(abstractEthRequest.getJsonrpc());

        try {
            EthRequest ethRequest = (EthRequest) abstractEthRequest;

            final Optional<String> accountAddressOptional = ethRequest.getParams()
                    .stream().findFirst();

            if(accountAddressOptional.isEmpty()) {
                throw new RPCException(RPCErrorCode.MALFORMED_PAYLOAD);
            }

            final String accountAddress = accountAddressOptional.get();

            Account account = Optional.ofNullable((Account) storages.getStorage(StorageTypes.STATE)
                            .get(ColumnFamilyTypes.ACCOUNTS, accountAddress.getBytes(StandardCharsets.UTF_8)))
                    .orElseGet(() -> Account.empty(accountAddress));

            final String accountNonce = "0x"+account.getNonce().toString(16);

            response.setResult(accountNonce);
        } catch (Exception e) {
            response.setResult("ERROR!");
        }

        log.info("Response:{}", response);

        return response;
    }

    private EthResponse sendRawTransaction(AbstractEthRequest abstractEthRequest) {
        EthResponse response = new EthResponse();
        response.setId(abstractEthRequest.getId());
        response.setJsonrpc(abstractEthRequest.getJsonrpc());

        try {
            EthRequest ethRequest = (EthRequest) abstractEthRequest;

            final Optional<String> rawData = ethRequest.getParams()
                    .stream().findFirst();

            if(rawData.isEmpty()) {
                throw new RPCException(RPCErrorCode.MALFORMED_PAYLOAD);
            }


            log.info("Real X:{}", new ECKey("82aa69f32308dd58f1b03c7708f7874d707c2f6afa707419c05e6f4a41369bd1").getPublicKey().toAddress());
        } catch (Exception e) {
            log.info("Error:{}", e);
            response.setResult("ERROR!");
        }

        return response;
    }

    private EthResponse processSendTransaction(AbstractEthRequest abstractEthRequest) {
        EthResponse response = new EthResponse();
        response.setId(abstractEthRequest.getId());
        response.setJsonrpc(abstractEthRequest.getJsonrpc());

        try {
            EthRequest ethRequest = (EthRequest) abstractEthRequest;

            final Optional<String> transactionDetailsOptional = ethRequest.getParams()
                    .stream().findFirst();

            if(transactionDetailsOptional.isEmpty()) {
                throw new RPCException(RPCErrorCode.MALFORMED_PAYLOAD);
            }

            final String deserializeJSONData = new String(HashingService
                    .hexStringToByteArray(transactionDetailsOptional.get()), StandardCharsets.UTF_8);

            final MempoolTransaction mempoolTransaction = MempoolTransaction.create(deserializeJSONData);

            if(mempoolTransaction.verifyTransaction()) {
                response.setResult(mempoolTransaction.getHash());
                Sinks.EmitResult result = mempoolSink.tryEmitNext(mempoolTransaction);
                if(result.isSuccess()) {
                    log.info("Added to the sink!");
                }
            }
            response.setResult(mempoolTransaction.getHash());
        } catch (Exception e) {
            log.info("Error:{}", e);
            response.setResult("ERROR!");
        }

        return response;
    }
}
