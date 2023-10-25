package uk.co.roteala.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.roteala.api.eth.EthRequest;
import uk.co.roteala.api.eth.EthResponse;
import uk.co.roteala.services.StorageServices;
import uk.co.roteala.storage.Storages;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Blockchain Transaction Operations", description = "The API to fetch info regarding transaction")
public class ETHController {
    @Autowired
    private final StorageServices storageServices;
    @PostMapping("/")
    public EthResponse handleEthRequest(@RequestBody String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        EthRequest request = null;

        // Attempt to deserialize
        try {
            request = mapper.readValue(jsonString, EthRequest.class);
        } catch (JsonProcessingException e) {
            log.info("Invalid Request Format: {}", jsonString);
            return new EthResponse(null, null, "Invalid Request Format");
        }

        // Handle valid EthRequest
        switch (request.getMethod()) {
            case "eth_chainId":
                return new EthResponse(request.getId(), request.getJsonrpc(), "0x1ca3");
            case "eth_blockNumber":
                return new EthResponse(request.getId(), request.getJsonrpc(), "latest");
            case "eth_getBalance":
                return new EthResponse(request.getId(), request.getJsonrpc(), "0x0234c8a3397aab58");
            case "net_version":
                return new EthResponse(request.getId(), request.getJsonrpc(), "0x1");
            default:
                return new EthResponse(request.getId(), request.getJsonrpc(), "Method not supported");
        }
    }
}
