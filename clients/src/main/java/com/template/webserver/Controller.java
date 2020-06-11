package com.template.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.flows.Initiator;
import com.template.states.TransactionState;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * Defining API endpoints
 */
@RestController
@RequestMapping("/api/recon") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    private final CordaX500Name me;

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.getProxy();

        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    @Configuration
    class Plugin {
        @Bean
        public ObjectMapper registerModule() {
            return JacksonSupport.createNonRpcMapper();
        }
    }

    @GetMapping(value = "/me",produces = APPLICATION_JSON_VALUE)
    private HashMap<String, String> whoami(){
        HashMap<String, String> myMap = new HashMap<>();
        myMap.put("me", me.toString());
        return myMap;
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isMe(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);
    }

    private boolean isNetworkMap(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    @GetMapping(value = "/peers", produces = APPLICATION_JSON_VALUE)
    public HashMap<String, List<String>> getPeers() {
        HashMap<String, List<String>> myMap = new HashMap<>();

        // Find all nodes that are not notaries, ourself, or the network map.
        Stream<NodeInfo> filteredNodes = proxy.networkMapSnapshot().stream()
                .filter(el -> !isNotary(el) && !isMe(el) && !isNetworkMap(el));
        // Get their names as strings
        List<String> nodeNames = filteredNodes.map(el -> el.getLegalIdentities().get(0).getName().toString())
                .collect(Collectors.toList());

        myMap.put("peers", nodeNames);
        return myMap;
    }

    @RequestMapping(value="/index",method = RequestMethod.GET)
    private String templateendpoint() {
        System.out.println("hello");
        return "index";
    }

    @GetMapping(value = "/transactions",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<TransactionState>> getTransactions() {
        return proxy.vaultQuery(TransactionState.class).getStates();
    }

    @GetMapping(value =  "/start-reconciliation" , produces = TEXT_PLAIN_VALUE )
    public ResponseEntity<String> startReconciliation(@RequestParam(value = "merchantAccountNumber") String merchantAccountNumber,
                                           @RequestParam(value = "currency") String currency,
                                           @RequestParam(value = "amount") int amount,
                                           @RequestParam(value = "receiverAccountNumber") String receiverAccountNumber) throws IllegalArgumentException {
        logger.info("REQUEST ACCEPTED");
        Amount<Currency> parsedAmount = new Amount<>((long) amount * 100, Currency.getInstance(currency));
        UUID txnUUID = UUID.randomUUID();
        String parsedUUID = txnUUID.toString();
        try {

            SignedTransaction result = proxy.startTrackedFlowDynamic(Initiator.class, parsedUUID,parsedAmount,merchantAccountNumber,receiverAccountNumber).getReturnValue().get();

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id "+ result.getId() +" committed to ledger.\n " + result.getTx().getOutput(0));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}