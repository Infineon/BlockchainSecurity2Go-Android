package co.coinfinity.infineonandroidapp.ethereum.contract;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint8;
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
    public static final String FUNC_GETVOTERSNAME = "getVotersName";

    public static final String FUNC_NUMBER_OF_POSSIBLE_CHOICES = "numberOfPossibleChoices";

    public static final String FUNC_CAST_VOTE = "castVote";

    public static final String FUNC_GETVOTERSANSWER = "getVotersChoice";

    public static final String FUNC_GET_CURRENT_RESULT = "getCurrentResult";

    public static final String FUNC_VOTERS = "voters";
    private static final String BINARY = "608060405234801561001057600080fd5b50604051602080610a8083398101604081815291516000805433600160a060020a03199091161760a060020a60ff0219167401000000000000000000000000000000000000000060ff8085168202929092179283905590910416808352602080820284010190935291801561008f578160200160208202803883390190505b5080516100a4916003916020909101906100ab565b5050610172565b82805482825590600052602060002090601f016020900481019282156101415791602002820160005b8382111561011257835183826101000a81548160ff021916908360ff16021790555092602001926001016020816000010492830192600103026100d4565b801561013f5782816101000a81549060ff0219169055600101602081600001049283019260010302610112565b505b5061014d929150610151565b5090565b61016f91905b8082111561014d57805460ff19168155600101610157565b90565b6108ff806101816000396000f3006080604052600436106100825763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630e7b80ee8114610087578063150a5655146100b257806341c0e1b5146101245780635d69f68c1461013b57806365b7b357146101c5578063a3ec138d146101da578063fa2073ad14610298575b600080fd5b34801561009357600080fd5b5061009c6102fd565b6040805160ff9092168252519081900360200190f35b3480156100be57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526101109436949293602493928401919081908401838280828437509497505050923560ff16935061031e92505050565b604080519115158252519081900360200190f35b34801561013057600080fd5b506101396104ef565b005b34801561014757600080fd5b50610150610579565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561018a578181015183820152602001610172565b50505050905090810190601f1680156101b75780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b3480156101d157600080fd5b5061009c61068c565b3480156101e657600080fd5b5061020873ffffffffffffffffffffffffffffffffffffffff60043516610718565b60405180806020018460ff1660ff16815260200183151515158152602001828103825285818151815260200191508051906020019080838360005b8381101561025b578181015183820152602001610243565b50505050905090810190601f1680156102885780820380516001836020036101000a031916815260200191505b5094505050505060405180910390f35b3480156102a457600080fd5b506102ad6107c6565b60408051602080825283518183015283519192839290830191858101910280838360005b838110156102e95781810151838201526020016102d1565b505050509050019250505060405180910390f35b60005474010000000000000000000000000000000000000000900460ff1681565b6000805460609060ff7401000000000000000000000000000000000000000090910481169084161061034f57600080fd5b50825183906002106103ab576040805160e560020a62461bcd02815260206004820181905260248201527f4e616d65206f6620766f7465722068617320746f206c6573732063686172732e604482015290519081900360640190fd5b3360009081526001602081905260409091200154610100900460ff161561041c576040805160e560020a62461bcd02815260206004820152600e60248201527f416c726561647920766f7465642e000000000000000000000000000000000000604482015290519081900360640190fd5b6040805160608101825285815260ff85166020808301919091526001828401819052336000908152908252929092208151805192939192610460928492019061083b565b5060208201516001918201805460409094015160ff1990941660ff9283161761ff001916610100941515949094029390931790925560028054909101905560038054909185169081106104af57fe5b60009182526020918290209181049091018054600160ff601f9094166101000a808304851682018516810294021990911692909217905591505092915050565b60005473ffffffffffffffffffffffffffffffffffffffff16331461055e576040805160e560020a62461bcd02815260206004820152601e60248201527f4e6f7420746865206f776e6572206f662074686520636f6e74726163742e0000604482015290519081900360640190fd5b60005473ffffffffffffffffffffffffffffffffffffffff16ff5b3360009081526001602081905260409091200154606090610100900460ff1615156105ee576040805160e560020a62461bcd02815260206004820152600f60248201527f4e6f20766f746520736f206661722e0000000000000000000000000000000000604482015290519081900360640190fd5b3360009081526001602081815260409283902080548451600294821615610100026000190190911693909304601f81018390048302840183019094528383529192908301828280156106815780601f1061065657610100808354040283529160200191610681565b820191906000526020600020905b81548152906001019060200180831161066457829003601f168201915b505050505090505b90565b33600090815260016020819052604082200154610100900460ff1615156106fd576040805160e560020a62461bcd02815260206004820152600f60248201527f4e6f20766f746520736f206661722e0000000000000000000000000000000000604482015290519081900360640190fd5b50336000908152600160208190526040909120015460ff1690565b60016020818152600092835260409283902080548451600294821615610100026000190190911693909304601f81018390048302840183019094528383529283918301828280156107aa5780601f1061077f576101008083540402835291602001916107aa565b820191906000526020600020905b81548152906001019060200180831161078d57829003601f168201915b5050506001909301549192505060ff8082169161010090041683565b6060600380548060200260200160405190810160405280929190818152602001828054801561068157602002820191906000526020600020906000905b825461010083900a900460ff168152602060019283018181049485019490930390920291018084116108035790505050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061087c57805160ff19168380011785556108a9565b828001600101855582156108a9579182015b828111156108a957825182559160200191906001019061088e565b506108b59291506108b9565b5090565b61068991905b808211156108b557600081556001016108bf5600a165627a7a72305820afdcf56ed7880b5d4c36e469bd452e2370d118b4720600d805c02d727ba1ef960029";

    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, Uint8 initMaxAnswers) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(initMaxAnswers));
        return deployRemoteCall(Voting.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, Uint8 initMaxAnswers) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(initMaxAnswers));
        return deployRemoteCall(Voting.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<Uint8> maxAnswers() {
        final Function function = new Function(FUNC_NUMBER_OF_POSSIBLE_CHOICES,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> castVote(Utf8String voterName, Uint8 answer) {
        final Function function = new Function(
                FUNC_CAST_VOTE,
                Arrays.<Type>asList(voterName, answer),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Utf8String> getVotersName() {
        final Function function = new Function(FUNC_GETVOTERSNAME,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Uint8> getVotersAnswer() {
        final Function function = new Function(FUNC_GETVOTERSANSWER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Tuple3<Utf8String, Uint8, Bool>> voters(Address param0) {
        final Function function = new Function(FUNC_VOTERS,
                Arrays.<Type>asList(param0),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }, new TypeReference<Uint8>() {
                }, new TypeReference<Bool>() {
                }));
        return new RemoteCall<Tuple3<Utf8String, Uint8, Bool>>(
                new Callable<Tuple3<Utf8String, Uint8, Bool>>() {
                    @Override
                    public Tuple3<Utf8String, Uint8, Bool> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<Utf8String, Uint8, Bool>(
                                (Utf8String) results.get(0),
                                (Uint8) results.get(1),
                                (Bool) results.get(2));
                    }
                });
    }

    public RemoteCall<DynamicArray<Uint32>> getCurrentResult() {
        final Function function = new Function(FUNC_GET_CURRENT_RESULT,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint8>>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }
}
