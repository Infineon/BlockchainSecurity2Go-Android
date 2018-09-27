package co.coinfinity.infineonandroidapp.ethereum.contract;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint40;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.Log;
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
 * <p>Generated with web3j version 3.5.0.
 */
public class Voting extends Contract {
    public static final String FUNC_THISVOTEREXISTS = "thisVoterExists";

    public static final String FUNC_VOTERSINFO = "votersInfo";

    public static final String FUNC_RECOVERTOKENS = "recoverTokens";

    public static final String FUNC_CASTVOTE = "castVote";

    public static final String FUNC_THISVOTERSNAME = "thisVotersName";

    public static final String FUNC_NUMBEROFCHOICES = "numberOfChoices";

    public static final String FUNC_VOTECOUNTTOTAL = "voteCountTotal";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";
    private static final String BINARY = "608060405234801561001057600080fd5b50604051602080611368833981016040525160008054600160a060020a03191633179055600260ff821610156100a757604080517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152601a60248201527f4d696e696d756d20322063686f6963657320616c6c6f7765642e000000000000604482015290519081900360640190fd5b60ff818116111561011957604080517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152601c60248201527f4d6178696d756d203235352063686f6963657320616c6c6f7765642e00000000604482015290519081900360640190fd5b60ff811661012860018261012f565b5050610189565b8154818355818111156101635760070160089004816007016008900483600052602060002091820191016101639190610168565b505050565b61018691905b80821115610182576000815560010161016e565b5090565b90565b6111d0806101986000396000f3006080604052600436106100e55763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630ee4dc5a811461014457806316114acd146101f557806333b3dc04146102165780633b7a3b7f1461023e5780634d14e344146102c85780635961e4fb146102f3578063715018a61461032257806379a3510d1461033757806383197ef0146103605780638da5cb5b146103755780638f32d59b146103a6578063a9f16529146103bb578063ab6f9e4a146103d0578063c6c4676c14610404578063f2fde38b14610469578063f5074f411461048a575b3480156100f157600080fd5b506040805160e560020a62461bcd02815260206004820181905260248201527f46616c6c6261636b2066756e6374696f6e20616c77617973207468726f77732e604482015290519081900360640190fd5b005b34801561015057600080fd5b50610165600160a060020a03600435166104ab565b60405180841515151581526020018360ff1660ff16815260200180602001828103825283818151815260200191508051906020019080838360005b838110156101b85781810151838201526020016101a0565b50505050905090810190601f1680156101e55780820380516001836020036101000a031916815260200191505b5094505050505060405180910390f35b34801561020157600080fd5b50610142600160a060020a036004351661055a565b34801561022257600080fd5b50610142602460048035828101929101359060ff903516610766565b34801561024a57600080fd5b50610253610b02565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561028d578181015183820152602001610275565b50505050905090810190601f1680156102ba5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b3480156102d457600080fd5b506102dd610c0c565b6040805160ff9092168252519081900360200190f35b3480156102ff57600080fd5b50610308610c12565b6040805164ffffffffff9092168252519081900360200190f35b34801561032e57600080fd5b50610142610c37565b34801561034357600080fd5b5061034c610cd8565b604080519115158252519081900360200190f35b34801561036c57600080fd5b50610142610cee565b34801561038157600080fd5b5061038a610d4d565b60408051600160a060020a039092168252519081900360200190f35b3480156103b257600080fd5b5061034c610d5c565b3480156103c757600080fd5b506102dd610d6d565b3480156103dc57600080fd5b506103eb60ff60043516610df2565b6040805163ffffffff9092168252519081900360200190f35b34801561041057600080fd5b50610419610ebe565b60408051602080825283518183015283519192839290830191858101910280838360005b8381101561045557818101518382015260200161043d565b505050509050019250505060405180910390f35b34801561047557600080fd5b50610142600160a060020a0360043516610f41565b34801561049657600080fd5b50610142600160a060020a0360043516610f99565b60026020818152600092835260409283902080546001808301805487516101009382161584026000190190911696909604601f810186900486028701860190975286865260ff80841697929093049092169492938301828280156105505780601f1061052557610100808354040283529160200191610550565b820191906000526020600020905b81548152906001019060200180831161053357829003601f168201915b5050505050905083565b6000610564610d5c565b15156105a8576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611185833981519152604482015290519081900360640190fd5b604080517f70a082310000000000000000000000000000000000000000000000000000000081523060048201529051600160a060020a038416916370a082319160248083019260209291908290030181600087803b15801561060957600080fd5b505af115801561061d573d6000803e3d6000fd5b505050506040513d602081101561063357600080fd5b50519050600160a060020a03821663a9059cbb61064e610d4d565b836040518363ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018083600160a060020a0316600160a060020a0316815260200182815260200192505050602060405180830381600087803b1580156106ba57600080fd5b505af11580156106ce573d6000803e3d6000fd5b505050506040513d60208110156106e457600080fd5b50511515610762576040805160e560020a62461bcd02815260206004820152603160248201527f546f6b656e207472616e73666572206661696c65642c207472616e736665722860448201527f292072657475726e65642066616c73652e000000000000000000000000000000606482015290519081900360840190fd5b5050565b61076e610c0c565b60ff168160ff161015156107f2576040805160e560020a62461bcd02815260206004820152603d60248201527f43686f696365206d757374206265206c657373207468616e20636f6e7472616360448201527f7420636f6e66696775726564206e756d6265724f6643686f696365732e000000606482015290519081900360840190fd5b3360009081526002602052604090205460ff1615610880576040805160e560020a62461bcd02815260206004820152602c60248201527f5468697320616464726573732068617320616c726561647920766f7465642e2060448201527f566f74652064656e6965642e0000000000000000000000000000000000000000606482015290519081900360840190fd5b600282116108d8576040805160e560020a62461bcd02815260206004820152601b60248201527f4e616d65206f6620766f74657220697320746f6f2073686f72742e0000000000604482015290519081900360640190fd5b6060604051908101604052806001151581526020018260ff16815260200184848080601f01602080910402602001604051908101604052809392919081815260200183838082843750505092909352505033600090815260026020908152604091829020845181548684015160ff166101000261ff001992151560ff19909216919091179190911617815591840151805192935061097f92600185019291909101906110ec565b50506000546109af915074010000000000000000000000000000000000000000900464ffffffffff166001610ff1565b600060146101000a81548164ffffffffff021916908364ffffffffff160217905550610a1360018260ff168154811015156109e657fe5b90600052602060002090600891828204019190066004029054906101000a900463ffffffff16600161100e565b6001805460ff8416908110610a2457fe5b90600052602060002090600891828204019190066004026101000a81548163ffffffff021916908363ffffffff1602179055508060ff167f62d4a36c4ee5c9ccdd5f79563190de8917cec21d6b15519f9007fd02bbe999c8600160405180806020018281038252838181548152602001915080548015610aef57602002820191906000526020600020906000905b82829054906101000a900463ffffffff1663ffffffff1681526020019060040190602082600301049283019260010382029150808411610ab25790505b50509250505060405180910390a2505050565b3360009081526002602052604090205460609060ff161515610b6e576040805160e560020a62461bcd02815260206004820152600f60248201527f4e6f20766f746520736f206661722e0000000000000000000000000000000000604482015290519081900360640190fd5b336000908152600260208181526040928390206001908101805485519281161561010002600019011693909304601f81018390048302820183019094528381529290830182828015610c015780601f10610bd657610100808354040283529160200191610c01565b820191906000526020600020905b815481529060010190602001808311610be457829003601f168201915b505050505090505b90565b60015490565b60005474010000000000000000000000000000000000000000900464ffffffffff1681565b610c3f610d5c565b1515610c83576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611185833981519152604482015290519081900360640190fd5b60008054604051600160a060020a03909116917ff8df31144d9c2f0f6b59d69b8b98abd5459d07f2742c4df920b25aae33c6482091a26000805473ffffffffffffffffffffffffffffffffffffffff19169055565b3360009081526002602052604090205460ff1690565b610cf6610d5c565b1515610d3a576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611185833981519152604482015290519081900360640190fd5b610d42610d4d565b600160a060020a0316ff5b600054600160a060020a031690565b600054600160a060020a0316331490565b3360009081526002602052604081205460ff161515610dd6576040805160e560020a62461bcd02815260206004820152600f60248201527f4e6f20766f746520736f206661722e0000000000000000000000000000000000604482015290519081900360640190fd5b5033600090815260026020526040902054610100900460ff1690565b6000610dfc610c0c565b60ff168260ff16101515610e80576040805160e560020a62461bcd02815260206004820152603d60248201527f43686f696365206d757374206265206c657373207468616e20636f6e7472616360448201527f7420636f6e66696775726564206e756d6265724f6643686f696365732e000000606482015290519081900360840190fd5b6001805460ff8416908110610e9157fe5b90600052602060002090600891828204019190066004029054906101000a900463ffffffff169050919050565b60606001805480602002602001604051908101604052809291908181526020018280548015610c0157602002820191906000526020600020906000905b82829054906101000a900463ffffffff1663ffffffff1681526020019060040190602082600301049283019260010382029150808411610efb5790505050505050905090565b610f49610d5c565b1515610f8d576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611185833981519152604482015290519081900360640190fd5b610f9681611024565b50565b610fa1610d5c565b1515610fe5576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611185833981519152604482015290519081900360640190fd5b80600160a060020a0316ff5b81810164ffffffffff808416908216101561100857fe5b92915050565b81810163ffffffff808416908216101561100857fe5b600160a060020a0381161515611084576040805160e560020a62461bcd02815260206004820152601860248201527f4e6577206f776e65722063616e6e6f74206265203078302e0000000000000000604482015290519081900360640190fd5b60008054604051600160a060020a03808516939216917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e091a36000805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061112d57805160ff191683800117855561115a565b8280016001018555821561115a579182015b8281111561115a57825182559160200191906001019061113f565b5061116692915061116a565b5090565b610c0991905b80821115611166576000815560010161117056004f6e6c7920746865206f776e65722063616e20646f20746869732e0000000000a165627a7a7230582065d77a0e04e02cbc225193065357c59d24182d5eb6a48ee640ef736ea84a2a1b0029";

    public static final String FUNC_DESTROY = "destroy";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_ISOWNER = "isOwner";

    public static final String FUNC_THISVOTERSCHOICE = "thisVotersChoice";

    public static final String FUNC_VOTESPERCHOICE = "votesPerChoice";

    public static final String FUNC_CURRENTRESULT = "currentResult";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_DESTROYANDSEND = "destroyAndSend";

    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<Tuple3<Bool, Uint8, Utf8String>> votersInfo(Address param0) {
        final Function function = new Function(FUNC_VOTERSINFO, 
                Arrays.<Type>asList(param0), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint8>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteCall<Tuple3<Bool, Uint8, Utf8String>>(
                new Callable<Tuple3<Bool, Uint8, Utf8String>>() {
                    @Override
                    public Tuple3<Bool, Uint8, Utf8String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<Bool, Uint8, Utf8String>(
                                (Bool) results.get(0), 
                                (Uint8) results.get(1), 
                                (Utf8String) results.get(2));
                    }
                });
    }

    public RemoteCall<TransactionReceipt> recoverTokens(Address token) {
        final Function function = new Function(
                FUNC_RECOVERTOKENS, 
                Arrays.<Type>asList(token), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> castVote(Utf8String voterName, Uint8 givenVote) {
        final Function function = new Function(
                FUNC_CASTVOTE, 
                Arrays.<Type>asList(voterName, givenVote), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Utf8String> thisVotersName() {
        final Function function = new Function(FUNC_THISVOTERSNAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Uint8> numberOfChoices() {
        final Function function = new Function(FUNC_NUMBEROFCHOICES, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Uint40> voteCountTotal() {
        final Function function = new Function(FUNC_VOTECOUNTTOTAL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint40>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Bool> thisVoterExists() {
        final Function function = new Function(FUNC_THISVOTEREXISTS,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> destroy() {
        final Function function = new Function(
                FUNC_DESTROY, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Address> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Bool> isOwner() {
        final Function function = new Function(FUNC_ISOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Uint8> thisVotersChoice() {
        final Function function = new Function(FUNC_THISVOTERSCHOICE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Uint32> votesPerChoice(Uint8 option) {
        final Function function = new Function(FUNC_VOTESPERCHOICE, 
                Arrays.<Type>asList(option), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint32>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<DynamicArray<Uint32>> currentResult() {
        final Function function = new Function(FUNC_CURRENTRESULT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint32>>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> transferOwnership(Address newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(newOwner), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> destroyAndSend(Address _recipient) {
        final Function function = new Function(
                FUNC_DESTROYANDSEND, 
                Arrays.<Type>asList(_recipient), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, Uint8 initMaxChoices) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(initMaxChoices));
        return deployRemoteCall(Voting.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, Uint8 initMaxChoices) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(initMaxChoices));
        return deployRemoteCall(Voting.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class NewVoteEventResponse {
        public Log log;

        public Uint8 addedVote;

        public DynamicArray<Uint32> allVotes;
    }

    public static class OwnershipRenouncedEventResponse {
        public Log log;

        public Address previousOwner;
    }

    public static class OwnershipTransferredEventResponse {
        public Log log;

        public Address previousOwner;

        public Address newOwner;
    }
}
