package uk.co.roteala.rpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.roteala.api.eth.AbstractEthRequest;
import uk.co.roteala.api.eth.EthRequest;
import uk.co.roteala.api.eth.EthRequestWithParam;
import uk.co.roteala.api.eth.EthResponse;
import uk.co.roteala.common.Account;
import uk.co.roteala.common.ChainState;
import uk.co.roteala.common.storage.ColumnFamilyTypes;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.configs.BrokerConfigs;
import uk.co.roteala.exceptions.RPCException;
import uk.co.roteala.exceptions.errorcodes.RPCErrorCode;
import uk.co.roteala.storage.Storages;
import uk.co.roteala.utils.BlockchainUtils;
import uk.co.roteala.utils.Constants;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RPCServices {
    private final BrokerConfigs brokerConfigs;
    private final Storages storages;
    public EthResponse processRequest(AbstractEthRequest abstractEthRequest) {
        log.info("Request:{}", abstractEthRequest);
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

            final String accountAddress = accountAddressOptional.get();

            Account account = Optional.ofNullable((Account) storages.getStorage(StorageTypes.STATE)
                            .get(ColumnFamilyTypes.ACCOUNTS, accountAddress.getBytes(StandardCharsets.UTF_8)))
                    .orElseGet(() -> Account.empty(accountAddress));

            response.setResult(account.getBalance());
        } catch (Exception e) {
            log.info("Error:{}", e);
            response.setResult("ERROR!");
        }

        log.info("Response:{}", response);
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
                log.info("Details:{}", lastBlockIndex);
                response.setResult(lastBlockIndex);
            }
        } catch (Exception e) {
            response.setResult("ERROR!");
        }

        log.info("Response:{}", response);

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

            final String gasPrice = "0x2FAF080";
            response.setResult(gasPrice);
        } catch (Exception e) {
            response.setResult("ERROR!");
        }

        log.info("Response:{}", response);

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

        log.info("Response:{}", response);

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

            response.setResult(account.getNonce());
        } catch (Exception e) {
            response.setResult("ERROR!");
        }

        log.info("Response:{}", response);

        return response;
    }
}
