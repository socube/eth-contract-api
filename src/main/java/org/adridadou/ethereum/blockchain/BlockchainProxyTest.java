package org.adridadou.ethereum.blockchain;

import static org.ethereum.config.blockchain.FrontierConfig.FrontierConstants;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.smartcontract.SmartContractTest;
import org.adridadou.ethereum.values.*;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.util.blockchain.StandaloneBlockchain;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyTest implements BlockchainProxy {
    private final StandaloneBlockchain blockchain;

    public BlockchainProxyTest() {

        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        blockchain = new StandaloneBlockchain();
        blockchain.withAutoblock(true);
    }

    @Override
    public SmartContract map(SoliditySource src, String contractName, EthAddress address, EthAccount sender) {
        return new SmartContractTest(blockchain.createExistingContractFromSrc(src.getSource(), contractName, address.address));

    }

    @Override
    public SmartContract mapFromAbi(ContractAbi abi, EthAddress address, EthAccount sender) {
        return new SmartContractTest(blockchain.createExistingContractFromABI(abi.getAbi(), address.address));
    }

    @Override
    public CompletableFuture<EthAddress> publish(SoliditySource code, String contractName, EthAccount sender, Object... constructorArgs) {
        return CompletableFuture.completedFuture(EthAddress.of(blockchain.submitNewContract(code.getSource(), contractName, constructorArgs).getAddress()));
    }

    @Override
    public CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount sender, EthAddress address) {
        return null;
    }

    @Override
    public CompletableFuture<EthAddress> sendTx(EthValue ethValue, EthData data, EthAccount sender) {
        return this.sendTx(ethValue, data, sender, null).thenApply(result -> EthAddress.of(result.getResult()));
    }

    @Override
    public EthereumEventHandler events() {
        return null;
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return true;
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        return EthValue.wei(0);
    }

}
