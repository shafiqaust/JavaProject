package io.silverstring.core.provider.util.erc20;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;

@Service
public class Erc20ContractUtil {
    final Quorum ethereumWalletQuorumProxyProvider;

    @Autowired
    public Erc20ContractUtil(Quorum ethereumWalletQuorumProxyProvider) {
        this.ethereumWalletQuorumProxyProvider = ethereumWalletQuorumProxyProvider;
    }

    public String deploy(String BINARY,  List<String> privateFor, String fromAddress, BigInteger initialAmount, String tokenName, BigInteger decimalUnits, String tokenSymbol, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        try {
            TransactionManager transactionManager = new ClientTransactionManager(ethereumWalletQuorumProxyProvider, fromAddress, privateFor);
            HumanStandardToken humanStandardToken = HumanStandardToken.deploy(BINARY, ethereumWalletQuorumProxyProvider, transactionManager, gasPrice, gasLimit, initialAmount, tokenName, decimalUnits, tokenSymbol).send();
            return humanStandardToken.getContractAddress();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String name(String BINARY, String contractAddress, String fromAddress, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, gasPrice, gasLimit);
        try {
            return humanStandardToken.name().send();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionResponse<ApprovalEventResponse> approve(String BINARY, List<String> privateFor, String contractAddress, String fromAddress, String spender, BigInteger value, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, privateFor, gasPrice, gasLimit);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken.approve(spender, value).send();
            return processApprovalEventResponse(humanStandardToken, transactionReceipt);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long totalSupply(String BINARY, String contractAddress, String fromAddress, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, gasPrice, gasLimit);
        try {
            return extractLongValue(humanStandardToken.totalSupply().send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionResponse<TransferEventResponse> transferFrom(String BINARY, List<String> privateFor, String contractAddress, String from, String to, BigInteger value, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, from, privateFor, gasPrice, gasLimit);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken.transferFrom(from, to, value).send();
            return processTransferEventsResponse(humanStandardToken, transactionReceipt);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long decimals(String BINARY, String contractAddress, String fromAddress, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, gasPrice, gasLimit);
        try {
            return extractLongValue(humanStandardToken.decimals().send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String version(String BINARY, String contractAddress, String fromAddress, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, gasPrice, gasLimit);
        try {
            return humanStandardToken.version().send();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long balanceOf(String BINARY, String contractAddress, String fromAddress, String ownerAddress, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, gasPrice, gasLimit);
        try {
            return extractLongValue(humanStandardToken.balanceOf(ownerAddress).send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String symbol(String BINARY, String contractAddress, String fromAddress, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, gasPrice, gasLimit);
        try {
            return humanStandardToken.symbol().send();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionResponse<TransferEventResponse> transfer(String BINARY, List<String> privateFor, String contractAddress, String fromAddress, String to, BigInteger value, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, privateFor, gasPrice, gasLimit);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken.transfer(to, value).send();
            return processTransferEventsResponse(humanStandardToken, transactionReceipt);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionResponse<ApprovalEventResponse> approveAndCall(String BINARY, List<String> privateFor, String contractAddress, String fromAddress, String spender, BigInteger value, String extraData, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, privateFor, gasPrice, gasLimit);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken.approveAndCall(spender, value, extraData.getBytes()).send();
            return processApprovalEventResponse(humanStandardToken, transactionReceipt);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long allowance(String BINARY, String contractAddress, String fromAddress, String ownerAddress, String spenderAddress, BigInteger gasPrice, BigInteger gasLimit) throws Exception {
        HumanStandardToken humanStandardToken = load(BINARY, contractAddress, fromAddress, gasPrice, gasLimit);
        try {
            return extractLongValue(humanStandardToken.allowance(ownerAddress, spenderAddress).send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public HumanStandardToken load(String BINARY, String contractAddress, String fromAddress, List<String> privateFor, BigInteger gasPrice, BigInteger gasLimit) {
        TransactionManager transactionManager = new ClientTransactionManager(ethereumWalletQuorumProxyProvider, fromAddress, privateFor);
        return HumanStandardToken.load(BINARY, contractAddress, ethereumWalletQuorumProxyProvider, transactionManager, gasPrice, gasLimit);
    }

    public HumanStandardToken load(String BINARY, String contractAddress, String fromAddress, BigInteger gasPrice, BigInteger gasLimit) {
        TransactionManager transactionManager = new ClientTransactionManager(ethereumWalletQuorumProxyProvider, fromAddress, Collections.emptyList());
        return HumanStandardToken.load(BINARY, contractAddress, ethereumWalletQuorumProxyProvider, transactionManager, gasPrice, gasLimit);
    }

    private long extractLongValue(BigInteger value) {
        return value.longValueExact();
    }

    public TransactionResponse<ApprovalEventResponse> processApprovalEventResponse(HumanStandardToken humanStandardToken, TransactionReceipt transactionReceipt) {
        return processEventResponse(humanStandardToken.getApprovalEvents(transactionReceipt), transactionReceipt, ApprovalEventResponse::new);
    }

    public TransactionResponse<TransferEventResponse> processTransferEventsResponse(HumanStandardToken humanStandardToken, TransactionReceipt transactionReceipt) {
        return processEventResponse(humanStandardToken.getTransferEvents(transactionReceipt), transactionReceipt, TransferEventResponse::new);
    }

    public <T, R> TransactionResponse<R> processEventResponse(List<T> eventResponses, TransactionReceipt transactionReceipt, Function<T, R> map) {
        if (!eventResponses.isEmpty()) {
            return new TransactionResponse<>(transactionReceipt.getTransactionHash(), map.apply(eventResponses.get(0)));
        } else {
            return new TransactionResponse<>(transactionReceipt.getTransactionHash());
        }
    }

    @Data
    public static class TransferEventResponse {
        private String from;
        private String to;
        private long value;

        public TransferEventResponse() { }

        public TransferEventResponse(HumanStandardToken.TransferEventResponse transferEventResponse) {
            this.from = transferEventResponse._from;
            this.to = transferEventResponse._to;
            this.value = transferEventResponse._value.longValueExact();
        }
    }

    @Data
    public static class ApprovalEventResponse {
        private String owner;
        private String spender;
        private long value;

        public ApprovalEventResponse() { }

        public ApprovalEventResponse(HumanStandardToken.ApprovalEventResponse approvalEventResponse) {
            this.owner = approvalEventResponse._owner;
            this.spender = approvalEventResponse._spender;
            this.value = approvalEventResponse._value.longValueExact();
        }
    }

    @Data
    public static class TransactionResponse<T> {
        private String transactionHash;
        private T event;

        TransactionResponse() { }

        public TransactionResponse(String transactionHash) {
            this(transactionHash, null);
        }

        public TransactionResponse(String transactionHash, T event) {
            this.transactionHash = transactionHash;
            this.event = event;
        }
    }
}
