package co.coinfinity.infineonandroidapp.ethereum.contract;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Int8;
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
    public static final String FUNC_GETVOTE = "getVote";
    public static final String FUNC_MAXANSWERS = "maxAnswers";
    public static final String FUNC_GIVEVOTE = "giveVote";

    public static final String FUNC_KILL = "kill";
    private static final String BINARY = "608060405234801561001057600080fd5b506040516020806105d883398101604052516000805491810b60ff16740100000000000000000000000000000000000000000260a060020a60ff0219600160a060020a0319909316331792909216919091179055610565806100736000396000f30060806040526004361061006c5763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630242f35181146100715780630e7b80ee1461009f57806341c0e1b5146100b4578063652449e6146100cb578063a3ec138d1461013d575b600080fd5b34801561007d57600080fd5b506100866101fb565b60408051600092830b90920b8252519081900360200190f35b3480156100ab57600080fd5b5061008661023b565b3480156100c057600080fd5b506100c961025c565b005b3480156100d757600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526101299436949293602493928401919081908401838280828437509497505050923560000b935061029b92505050565b604080519115158252519081900360200190f35b34801561014957600080fd5b5061016b73ffffffffffffffffffffffffffffffffffffffff600435166103f0565b60405180806020018460000b60000b815260200183151515158152602001828103825285818151815260200191508051906020019080838360005b838110156101be5781810151838201526020016101a6565b50505050905090810190601f1680156101eb5780820380516001836020036101000a031916815260200191505b5094505050505060405180910390f35b33600090815260016020819052604082200154610100900460ff16151561022157600080fd5b5033600090815260016020819052604082200154900b5b90565b60008054740100000000000000000000000000000000000000009004900b81565b60005473ffffffffffffffffffffffffffffffffffffffff16331461028057600080fd5b60005473ffffffffffffffffffffffffffffffffffffffff16ff5b60008054606090740100000000000000000000000000000000000000009004820b820b83830b126102cb57600080fd5b508251839060021061033e57604080517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820181905260248201527f4e616d65206f6620766f7465722068617320746f206c6573732063686172732e604482015290519081900360640190fd5b3360009081526001602081905260409091200154610100900460ff161561036457600080fd5b60408051606081018252858152600085810b6020808401919091526001838501819052338352815292902081518051929391926103a492849201906104a1565b5060208201516001918201805460409094015115156101000261ff001960009390930b60ff1660ff19909516949094179190911692909217909155600280548201905591505092915050565b60016020818152600092835260409283902080548451600294821615610100026000190190911693909304601f81018390048302840183019094528383529283918301828280156104825780601f1061045757610100808354040283529160200191610482565b820191906000526020600020905b81548152906001019060200180831161046557829003601f168201915b50505060019093015491925050600081900b9060ff6101009091041683565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106104e257805160ff191683800117855561050f565b8280016001018555821561050f579182015b8281111561050f5782518255916020019190600101906104f4565b5061051b92915061051f565b5090565b61023891905b8082111561051b57600081556001016105255600a165627a7a72305820cd2cfc13a79a547471ccb68c1478e6cd1d289b06353c2e94832a8a4679c2094e0029";

    public static final String FUNC_VOTERS = "voters";

    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, Int8 initMaxAnswers) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(initMaxAnswers));
        return deployRemoteCall(Voting.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, Int8 initMaxAnswers) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(initMaxAnswers));
        return deployRemoteCall(Voting.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<TransactionReceipt> getVote() {
        final Function function = new Function(
                FUNC_GETVOTE,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Int8> maxAnswers() {
        final Function function = new Function(FUNC_MAXANSWERS,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int8>() {
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

    public RemoteCall<TransactionReceipt> giveVote(Utf8String voterName, Int8 answer) {
        final Function function = new Function(
                FUNC_GIVEVOTE,
                Arrays.<Type>asList(voterName, answer),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple3<Utf8String, Int8, Bool>> voters(Address param0) {
        final Function function = new Function(FUNC_VOTERS,
                Arrays.<Type>asList(param0),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }, new TypeReference<Int8>() {
                }, new TypeReference<Bool>() {
                }));
        return new RemoteCall<Tuple3<Utf8String, Int8, Bool>>(
                new Callable<Tuple3<Utf8String, Int8, Bool>>() {
                    @Override
                    public Tuple3<Utf8String, Int8, Bool> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<Utf8String, Int8, Bool>(
                                (Utf8String) results.get(0),
                                (Int8) results.get(1),
                                (Bool) results.get(2));
                    }
                });
    }
}
