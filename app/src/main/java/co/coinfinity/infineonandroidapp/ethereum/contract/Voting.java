package co.coinfinity.infineonandroidapp.ethereum.contract;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Int8;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.4.0.
 */
public class Voting extends Contract {
    public static final String FUNC_GET_STATUS = "get_status";
    public static final String FUNC_KILL = "kill";
    public static final String FUNC_GIVE_VOTE = "give_vote";
    public static final String FUNC_ANSWER_OPTIONS = "answer_options";
    public static final String FUNC_VOTERS = "voters";
    private static final String BINARY = "608060405234801561001057600080fd5b506040516020806105d283398101604052516000805491810b60ff16740100000000000000000000000000000000000000000260a060020a60ff0219600160a060020a031990931633179290921691909117905561055f806100736000396000f30060806040526004361061006c5763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166339aaba25811461007157806341c0e1b51461009a5780636505ef5d146100b157806394294a711461010f578063da58c7d91461013d575b600080fd5b34801561007d57600080fd5b506100866101f3565b604080519115158252519081900360200190f35b3480156100a657600080fd5b506100af61024f565b005b3480156100bd57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526100869436949293602493928401919081908401838280828437509497505050923560000b935061027292505050565b34801561011b57600080fd5b506101246103a9565b60408051600092830b90920b8252519081900360200190f35b34801561014957600080fd5b506101556004356103ca565b6040518084600160a060020a0316600160a060020a03168152602001806020018360000b60000b8152602001828103825284818151815260200191508051906020019080838360005b838110156101b657818101518382015260200161019e565b50505050905090810190601f1680156101e35780820380516001836020036101000a031916815260200191505b5094505050505060405180910390f35b600154600090815b8181101561024557600180543391908390811061021457fe5b6000918252602090912060039091020154600160a060020a0316141561023d576001925061024a565b6001016101fb565b600092505b505090565b600054600160a060020a031633141561027057600054600160a060020a0316ff5b565b60008054606090740100000000000000000000000000000000000000009004820b820b83830b13156102a757600091506103a2565b8390506003815110156102bd57600091506103a2565b604080516060810182523381526020808201878152600087810b94840194909452600180548082018083559190955283517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf66003909602958601805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a039092169190911781559151805191959293610378937fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf70192910190610498565b50604091909101516002909101805460009290920b60ff1660ff1990921691909117905550600191505b5092915050565b60008054740100000000000000000000000000000000000000009004900b81565b60018054829081106103d857fe5b600091825260209182902060039091020180546001808301805460408051601f6002600019968516156101000296909601909316949094049182018790048702840187019052808352600160a060020a0390931695509293909291908301828280156104855780601f1061045a57610100808354040283529160200191610485565b820191906000526020600020905b81548152906001019060200180831161046857829003601f168201915b5050506002909301549192505060000b83565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106104d957805160ff1916838001178555610506565b82800160010185558215610506579182015b828111156105065782518255916020019190600101906104eb565b50610512929150610516565b5090565b61053091905b80821115610512576000815560010161051c565b905600a165627a7a7230582030818a2e3c80f068521b7049a985cce004c15657e1f71c61f0b91e7b30447b530029";

    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, Int8 init_answer_options) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(init_answer_options));
        return deployRemoteCall(Voting.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, Int8 init_answer_options) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(init_answer_options));
        return deployRemoteCall(Voting.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<Bool> get_status() {
        final Function function = new Function(FUNC_GET_STATUS,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> kill() {
        final Function function = new Function(
                FUNC_KILL,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> give_vote(Utf8String voter_name, Int8 answer) {
        final Function function = new Function(
                FUNC_GIVE_VOTE,
                Arrays.<Type>asList(voter_name, answer),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Int8> answer_options() {
        final Function function = new Function(FUNC_ANSWER_OPTIONS,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int8>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Tuple3<Address, Utf8String, Int8>> voters(Uint256 param0) {
        final Function function = new Function(FUNC_VOTERS,
                Arrays.<Type>asList(param0),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }, new TypeReference<Utf8String>() {
                }, new TypeReference<Int8>() {
                }));
        return new RemoteCall<Tuple3<Address, Utf8String, Int8>>(
                new Callable<Tuple3<Address, Utf8String, Int8>>() {
                    @Override
                    public Tuple3<Address, Utf8String, Int8> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<Address, Utf8String, Int8>(
                                (Address) results.get(0),
                                (Utf8String) results.get(1),
                                (Int8) results.get(2));
                    }
                });
    }
}
