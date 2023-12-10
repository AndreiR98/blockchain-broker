package uk.co.roteala.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.roteala.api.eth.*;
import uk.co.roteala.rpc.RPCServices;
import uk.co.roteala.services.StorageServices;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Blockchain Transaction Operations", description = "The API to fetch info regarding transaction")
public class ETHController {
    @Autowired
    private final StorageServices storageServices;

    @Autowired
    private final RPCServices rpcServices;
    @PostMapping("/")
    public EthResponse handleEthRequest(@RequestBody String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        APIEthRequest request = null;

        // Attempt to deserialize
        try {
            AbstractEthRequest requestWrapper = mapper
                    .readValue(jsonString, AbstractEthRequest.class);

            EthResponse response =rpcServices.processRequest(requestWrapper);

            log.info("Response:{}", jsonString);
            return response;
        } catch (JsonProcessingException e) {
            log.info("Error:{}", e);
            log.info("Invalid Request Format: {}", jsonString);
            return new EthResponse(null, null, "Invalid Request Format");
        }

        // Handle valid APIEthRequest
//        switch (request.getMethod()) {
//            case "eth_chainId":
//                return new EthResponse(request.getId(), request.getJsonrpc(), "0x1ca3");
//            case "eth_blockNumber":
//                return new EthResponse(request.getId(), request.getJsonrpc(), "latest");
//            case "eth_getBalance":
//                return new EthResponse(request.getId(), request.getJsonrpc(), "0x0234c8a3397aab58");
//            case "net_version":
//                return new EthResponse(request.getId(), request.getJsonrpc(), "0x1");
//            case "eth_estimateGas":
//                return new EthResponse(request.getId(), request.getJsonrpc(), "0x9FDF42F6E48000");
//            default:
//                return new EthResponse(request.getId(), request.getJsonrpc(), "Method not supported");
//        }
    }
}
