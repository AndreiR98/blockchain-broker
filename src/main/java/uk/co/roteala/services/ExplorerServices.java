package uk.co.roteala.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.netty.Connection;
import uk.co.roteala.api.ResultStatus;
import uk.co.roteala.api.account.AccountRequest;
import uk.co.roteala.api.account.AccountResponse;
import uk.co.roteala.api.block.BlockRequest;
import uk.co.roteala.api.block.BlockResponse;
import uk.co.roteala.api.mempool.LatestMempoolResponse;
import uk.co.roteala.api.transaction.TransactionRequest;
import uk.co.roteala.api.transaction.TransactionResponse;
import uk.co.roteala.common.*;
import uk.co.roteala.common.messenger.*;
import uk.co.roteala.common.storage.ColumnFamilyTypes;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.exceptions.BlockException;
import uk.co.roteala.exceptions.TransactionException;
import uk.co.roteala.exceptions.errorcodes.StorageErrorCode;

import uk.co.roteala.exceptions.errorcodes.TransactionErrorCode;
import uk.co.roteala.net.ConnectionsStorage;
import uk.co.roteala.storage.Storages;
import uk.co.roteala.utils.BlockchainUtils;
import uk.co.roteala.utils.Constants;

import javax.validation.Valid;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static uk.co.roteala.security.utils.HashingService.bytesToHexString;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ExplorerServices {
    private final Storages storages;
    private final ConnectionsStorage connectionsStorage;
    private final Messenger messenger;

//    public ExplorerResponse processExplorerRequest(@Valid ExplorerRequest explorerRequest){
//        ExplorerResponse response = new ExplorerResponse();
//
//        log.info("Requested:{}", explorerRequest);
//
//        if(BlockchainUtils.validAddress(explorerRequest.getDataHash())) {
//            response.setStatus(ResultStatus.SUCCESS);
//            response.setAccountAddress(explorerRequest.getDataHash());
//        } else if (BlockchainUtils.validTransactionHash(explorerRequest.getDataHash())) {
//            if(storage.getTransactionByKey(explorerRequest.getDataHash()) != null) {
//                response.setStatus(ResultStatus.SUCCESS);
//                response.setTransactionHash(explorerRequest.getDataHash());
//            }
//        } else if (BlockchainUtils.validBlockAddress(explorerRequest.getDataHash())) {
//            if(storage.getBlockByHash(explorerRequest.getDataHash()) != null) {
//                Block block = storage.getBlockByHash(explorerRequest.getDataHash());
//
//                response.setStatus(ResultStatus.SUCCESS);
//                response.setBlockIndex(block.getHeader().getIndex());
//            }
//        } else if (BlockchainUtils.isInteger(explorerRequest.getDataHash())){
//            if(storage.getBlockByIndex(explorerRequest.getDataHash()) != null) {
//                response.setStatus(ResultStatus.SUCCESS);
//                response.setBlockIndex(Integer.parseInt(explorerRequest.getDataHash()));
//            }
//        } else {
//            response.setStatus(ResultStatus.ERROR);
//        }
//
//        return response;
//    }

    public TransactionResponse getTransactionByHash(@Valid TransactionRequest transactionRequest){
        TransactionResponse response = new TransactionResponse();

        try {
            Transaction transaction = (Transaction) this.storages.getStorage(StorageTypes.BLOCKCHAIN)
                    .get(ColumnFamilyTypes.TRANSACTIONS, transactionRequest.getTransactionHash().getBytes(StandardCharsets.UTF_8));

            if(transaction == null) {
                throw new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND);
            }

            response.setHash(transaction.getHash());
            response.setBlockNumber(transaction.getBlockNumber());
            response.setFrom(transaction.getFrom());
            response.setTo(transaction.getTo());
            response.setAmount(transaction.getAmount());
            response.setVersion(transaction.getVersion());
            response.setTransactionIndex(transaction.getTransactionIndex());
            response.setNetworkFees(transaction.getNetworkFees());
            response.setProcessingFees(transaction.getProcessingFees());
            response.setNonce(transaction.getNonce());
            response.setTimeStamp(transaction.getTimeStamp());
            response.setConfirmations(transaction.getConfirmations());
            response.setBlockTime(transaction.getBlockTime());
            response.setPubKeyHash(transaction.getPubKeyHash());
            response.setTransactionStatus(transaction.getStatus());

            response.setResult(ResultStatus.SUCCESS);

            return response;
        } catch (Exception e) {
            return TransactionResponse.builder()
                    .result(ResultStatus.ERROR)
                    .message(e.getMessage()).build();
        }
    }

    public BlockResponse getBlock(@Valid BlockRequest blockRequest){
        BlockResponse response = new BlockResponse();
        try {
            Block block = null;
            if(BlockchainUtils.isInteger(blockRequest.getIndex())){
                block = (Block) storages.getStorage(StorageTypes.BLOCKCHAIN)
                        .get(ColumnFamilyTypes.BLOCKS, blockRequest.getIndex().getBytes(StandardCharsets.UTF_8));
            }

            if(block == null) {
                throw new BlockException(StorageErrorCode.BLOCK_NOT_FOUND);
            }

            response.setBlockHash(block.getHash());
            response.setBlock(block);
            response.setTotalTransactions(block.getTransactions().size());
            response.setResult(ResultStatus.SUCCESS);

            return response;
        } catch (Exception e) {
            return BlockResponse.builder()
                    .result(ResultStatus.ERROR)
                    .message(e.getMessage()).build();
        }
    }

    public LatestMempoolResponse getLatestMempool(@Valid String page) {
        LatestMempoolResponse response = new LatestMempoolResponse();

        try {
            List<String> hashes = new ArrayList<>();

            if(BlockchainUtils.isInteger(page)) {

            }
        }catch (Exception e) {

        }

        return null;
    }

    public void testConnection() {
        Block genesisBlock = Constants.GENESIS_BLOCK;

        List<String> transactions = new ArrayList<>();
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");
        transactions.add("0bb11c15a6f7c04a55a08c9ab68ab8f138efc38d68daad10e5da1216e97716bd");
        transactions.add("955c4ad045500cd37e789efda711e3153f9e2190c33633d8d67c93ffe33b0a22");
        transactions.add("29e67358bb1776e916869d5b0bce035fd51dc6048239daecaa206864fbeb9944");
        transactions.add("04d08570756d7f1e014401893f76d9728431bcdc5424b04e4789f3638ee93c28");
        transactions.add("29db5a27a4099ddaa18702f1596e614443e487b46a40cdbedc12b70455ae7738");
        transactions.add("66c157147be2c184bae1a8b780c8309b5e89ede768da7d8dcd08c63d905f3794");
        transactions.add("7916a7d22482d6174979b7eb4f696de29d28cea0a8ac5f1358fb1077868625ae");
        transactions.add("f09251bceb81f9d345ce907c28a1aedbbe63455ad625e618f3cf78fe7170c3ca");
        transactions.add("7a4d688b900271595fc3e27ad77aa3222d344dc2c48ca5d6738d5f9595ebc38a");
        transactions.add("9dea22818b904c412b90ad299539d6ce3af5cb74d0c405d16d38c60fadc25b72");
        transactions.add("eb5a24877f3fee260f7e2cbe0a4c3fcfd6ea1ff521a11ddd0b8fcadb5c94e0fa");
        transactions.add("9536153612f0b303e9fe645c33fb0a3f54ceee50c1a410d0aa298d780bb740bc");
        transactions.add("53f67fa417deebdd4f662035c1064cbceb14386d5467a9efa9e3bb50f849eeaa");

        genesisBlock.setTransactions(transactions);
    }

    public AccountResponse getAccount(@Valid AccountRequest accountRequest){
        AccountResponse response = new AccountResponse();

        try {
            final String accountAddress = accountRequest.getAddress().toLowerCase();

            Account account = Optional.ofNullable((Account) this.storages.getStorage(StorageTypes.STATE)
                            .get(ColumnFamilyTypes.ACCOUNTS, accountAddress.getBytes(StandardCharsets.UTF_8)))
                    .orElseGet(() -> Account.empty(accountAddress));

            response.setAddress(account.getAddress());
            response.setBalance(account.getBalance().toString());
            response.setVirtualBalance(account.getVirtualBalance().toString());
            response.setTransactionsIn(account.getTransactionsIn());
            response.setTransactionsOut(account.getTransactionsOut());

            response.setResult(ResultStatus.SUCCESS);
        } catch (Exception e) {
            response.setResult(ResultStatus.ERROR);
        }

        return response;
    }
//
//    public MempoolBlocksResponse getMempoolBlocksGrouped(){
//        MempoolBlocksResponse response = new MempoolBlocksResponse();
//
//        try {
//            final List<Block> pseudoBlocks = storage.getPseudoBlocks();
//
//            Map<Integer, List<Block>> mapByIndex = new HashMap<>();
//
//            pseudoBlocks.forEach(block -> {
//                int index = block.getHeader().getIndex();
//                mapByIndex.computeIfAbsent(index, k -> new ArrayList<>())
//                        .add(block);
//            });
//
//            response.setBlocksMap(mapByIndex);
//            response.setResult(ResultStatus.SUCCESS);
//        } catch (Exception e) {
//            response.setResult(ResultStatus.ERROR);
//            response.setMessage(e.getMessage());
//            throw new StorageException(StorageErrorCode.MEMPOOL_FAILED);
//        }
//
//        return response;
//    }
//
//    public BlockResponse getMempoolBlock(@Valid BlockRequest blockRequest){
//        BlockResponse response = new BlockResponse();
//
//        try {
//            Block block = null;
//            block = storage.getPseudoBlockByHash(blockRequest.getIndex());
//
//
//            if(block == null) {
//                throw new BlockException(StorageErrorCode.BLOCK_NOT_FOUND);
//            }
//
//            response.setBlockHash(block.getHash());
//            response.setBlock(block);
//            response.setTotalTransactions(block.getTransactions().size());
//            response.setResult(ResultStatus.SUCCESS);
//
//            return response;
//        } catch (Exception e) {
//            return BlockResponse.builder()
//                    .result(ResultStatus.ERROR)
//                    .message(e.getMessage()).build();
//        }
//    }
//
//    public PseudoTransactionResponse getPseudoTransaction(@Valid TransactionRequest transactionRequest) {
//        PseudoTransactionResponse response = new PseudoTransactionResponse();
//
//        try {
//            PseudoTransaction transaction = storage.getMempoolTransaction(transactionRequest.getTransactionHash());
//
//            if(transaction == null) {
//                throw new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND);
//            }
//
////            response.setPseudoHash(transaction.getPseudoHash());
////            response.setFrom(transaction.getFrom());
////            response.setTo(transaction.getTo());
////            response.setValue(transaction.getValue());
////            response.setVersion(transaction.getVersion());
////            response.setNonce(transaction.getNonce());
////            response.setTransactionStatus(transaction.getStatus());
//            response.setPseudoTransaction(transaction);
//
//            response.setResult(ResultStatus.SUCCESS);
//
//            return response;
//        } catch (Exception e) {
//            return PseudoTransactionResponse.builder()
//                    .result(ResultStatus.ERROR)
//                    .message(e.getMessage()).build();
//        }
//    }
}
