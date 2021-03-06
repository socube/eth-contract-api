package org.adridadou;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.integration.PrivateEthereumFacadeProvider;
import org.adridadou.ethereum.integration.PrivateNetworkConfig;
import org.adridadou.ethereum.provider.*;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;

import static org.adridadou.ethereum.values.EthValue.*;
import static org.junit.Assert.*;

import org.adridadou.ethereum.values.SoliditySource;
import org.adridadou.exception.EthereumApiException;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class TestnetConnectionTest {
    private final StandaloneEthereumFacadeProvider standalone = new StandaloneEthereumFacadeProvider();
    private final TestnetEthereumFacadeProvider testnet = new TestnetEthereumFacadeProvider();
    private final MordenEthereumFacadeProvider morden = new MordenEthereumFacadeProvider();
    private final MainEthereumFacadeProvider main = new MainEthereumFacadeProvider();
    private final PrivateEthereumFacadeProvider privateNetwork = new PrivateEthereumFacadeProvider();

    private EthAccount sender;
    private EthereumFacade ethereum;

    private void init() throws Exception {
        sender = privateNetwork.getKey("cow").decode("");
        ethereum = standalone.create();
        //privateNetwork.create(PrivateNetworkConfig.config()
        //.initialBalance(sender, ether(10)));
    }

    private MyContract2 publishAndMapContract() throws Exception {
        SoliditySource contract = SoliditySource.from(new File(this.getClass().getResource("/contract.sol").toURI()));
        CompletableFuture<EthAddress> futureAddress = ethereum.publishContract(contract, "myContract2", sender);
        EthAddress address = futureAddress.get();

        return ethereum.createContractProxy(contract, "myContract2", address, sender, MyContract2.class);
    }

    private void testMethodCalls(MyContract2 myContract) throws Exception {
        assertEquals("", myContract.getI1());
        System.out.println("*** calling contract myMethod");
        Future<Integer> future = myContract.myMethod("this is a test");
        Future<Integer> future2 = myContract.myMethod("this is a test2");
        assertEquals(12, future.get().intValue());
        assertEquals(12, future2.get().intValue());
        assertEquals("this is a test2", myContract.getI1());
        assertTrue(myContract.getT());

        Integer[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        assertArrayEquals(expected, myContract.getArray().toArray(new Integer[10]));

        assertArrayEquals(expected, myContract.getSet().toArray(new Integer[10]));

        assertEquals(new MyReturnType(true, "hello", 34), myContract.getM());

        assertEquals("", myContract.getI2());
        System.out.println("*** calling contract myMethod2 async");
        myContract.myMethod2("async call").get();

        assertEquals("async call", myContract.getI2());

        assertEquals(EnumTest.VAL2, myContract.getEnumValue());
        try {
            myContract.throwMe().get();
            fail("the call should fail!");
        } catch (final ExecutionException ex) {
            assertEquals(EthereumApiException.class, ex.getCause().getClass());
        }

    }

    @Test
    public void main_example_how_the_lib_works() throws Exception {
        init();
        MyContract2 myContract = publishAndMapContract();
        testMethodCalls(myContract);
    }

    public static class MyReturnType {
        private final Boolean val1;
        private final String val2;
        private final Integer val3;

        public MyReturnType(Boolean val1, String val2, Integer val3) {
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
        }

        @Override
        public String toString() {
            return "MyReturnType{" +
                    "val1=" + val1 +
                    ", val2='" + val2 + '\'' +
                    ", val3=" + val3 +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyReturnType that = (MyReturnType) o;

            if (val1 != null ? !val1.equals(that.val1) : that.val1 != null) return false;
            if (val2 != null ? !val2.equals(that.val2) : that.val2 != null) return false;
            return val3 != null ? val3.equals(that.val3) : that.val3 == null;

        }

        @Override
        public int hashCode() {
            int result = val1 != null ? val1.hashCode() : 0;
            result = 31 * result + (val2 != null ? val2.hashCode() : 0);
            result = 31 * result + (val3 != null ? val3.hashCode() : 0);
            return result;
        }
    }

    private enum EnumTest {
        VAL1, VAL2, VAL3
    }

    private interface MyContract2 {
        CompletableFuture<Integer> myMethod(String value);

        CompletableFuture<Void> myMethod2(String value);

        EnumTest getEnumValue();

        String getI1();

        String getI2();

        boolean getT();

        MyReturnType getM();

        List<Integer> getArray();

        Set<Integer> getSet();

        CompletableFuture<Void> throwMe();

    }
}
