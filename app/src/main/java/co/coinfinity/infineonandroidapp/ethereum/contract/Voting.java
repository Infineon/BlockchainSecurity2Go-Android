package co.coinfinity.infineonandroidapp.ethereum.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.StaticArray4;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint40;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.6.0.
 */
public class Voting extends Contract {
    private static final String BINARY = "608060405260008054600160a060020a03191633179055611152806100256000396000f3006080604052600436106100e55763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630ee4dc5a81146100f457806316114acd146101a557806333b3dc04146101c85780633b7a3b7f146101f05780634d14e3441461027a5780635961e4fb146102a5578063715018a6146102d457806379a3510d146102e957806383197ef0146103125780638da5cb5b146103275780638f32d59b14610358578063a9f165291461036d578063ab6f9e4a14610382578063c6c4676c146103b6578063f2fde38b14610403578063f5074f4114610424575b3480156100f157600080fd5b50005b34801561010057600080fd5b50610115600160a060020a0360043516610445565b60405180841515151581526020018360ff1660ff16815260200180602001828103825283818151815260200191508051906020019080838360005b83811015610168578181015183820152602001610150565b50505050905090810190601f1680156101955780820380516001836020036101000a031916815260200191505b5094505050505060405180910390f35b3480156101b157600080fd5b506101c6600160a060020a03600435166104f4565b005b3480156101d457600080fd5b506101c6602460048035828101929101359060ff903516610700565b3480156101fc57600080fd5b50610205610a8e565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561023f578181015183820152602001610227565b50505050905090810190601f16801561026c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561028657600080fd5b5061028f610b98565b6040805160ff9092168252519081900360200190f35b3480156102b157600080fd5b506102ba610b9d565b6040805164ffffffffff9092168252519081900360200190f35b3480156102e057600080fd5b506101c6610bc2565b3480156102f557600080fd5b506102fe610c63565b604080519115158252519081900360200190f35b34801561031e57600080fd5b506101c6610c79565b34801561033357600080fd5b5061033c610cd8565b60408051600160a060020a039092168252519081900360200190f35b34801561036457600080fd5b506102fe610ce7565b34801561037957600080fd5b5061028f610cf8565b34801561038e57600080fd5b5061039d60ff60043516610d7d565b6040805163ffffffff9092168252519081900360200190f35b3480156103c257600080fd5b506103cb610e3e565b6040518082608080838360005b838110156103f05781810151838201526020016103d8565b5050505090500191505060405180910390f35b34801561040f57600080fd5b506101c6600160a060020a0360043516610ea4565b34801561043057600080fd5b506101c6600160a060020a0360043516610efc565b60026020818152600092835260409283902080546001808301805487516101009382161584026000190190911696909604601f810186900486028701860190975286865260ff80841697929093049092169492938301828280156104ea5780601f106104bf576101008083540402835291602001916104ea565b820191906000526020600020905b8154815290600101906020018083116104cd57829003601f168201915b5050505050905083565b60006104fe610ce7565b1515610542576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611107833981519152604482015290519081900360640190fd5b604080517f70a082310000000000000000000000000000000000000000000000000000000081523060048201529051600160a060020a038416916370a082319160248083019260209291908290030181600087803b1580156105a357600080fd5b505af11580156105b7573d6000803e3d6000fd5b505050506040513d60208110156105cd57600080fd5b50519050600160a060020a03821663a9059cbb6105e8610cd8565b836040518363ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018083600160a060020a0316600160a060020a0316815260200182815260200192505050602060405180830381600087803b15801561065457600080fd5b505af1158015610668573d6000803e3d6000fd5b505050506040513d602081101561067e57600080fd5b505115156106fc576040805160e560020a62461bcd02815260206004820152603160248201527f546f6b656e207472616e73666572206661696c65642c207472616e736665722860448201527f292072657475726e65642066616c73652e000000000000000000000000000000606482015290519081900360840190fd5b5050565b610708610b98565b60ff168160ff1610151561078c576040805160e560020a62461bcd02815260206004820152603d60248201527f43686f696365206d757374206265206c657373207468616e20636f6e7472616360448201527f7420636f6e66696775726564206e756d6265724f6643686f696365732e000000606482015290519081900360840190fd5b3360009081526002602052604090205460ff161561081a576040805160e560020a62461bcd02815260206004820152602c60248201527f5468697320616464726573732068617320616c726561647920766f7465642e2060448201527f566f74652064656e6965642e0000000000000000000000000000000000000000606482015290519081900360840190fd5b60028211610872576040805160e560020a62461bcd02815260206004820152601b60248201527f4e616d65206f6620766f74657220697320746f6f2073686f72742e0000000000604482015290519081900360640190fd5b6060604051908101604052806001151581526020018260ff16815260200184848080601f01602080910402602001604051908101604052809392919081815260200183838082843750505092909352505033600090815260026020908152604091829020845181548684015160ff166101000261ff001992151560ff199092169190911791909116178155918401518051929350610919926001850192919091019061104f565b5050600054610949915074010000000000000000000000000000000000000000900464ffffffffff166001610f54565b6000805464ffffffffff92909216740100000000000000000000000000000000000000000278ffffffffff0000000000000000000000000000000000000000199092169190911790556109c8600160ff8316600481106109a557fe5b600891828204019190066004029054906101000a900463ffffffff166001610f71565b600160ff8316600481106109d857fe5b600891828204019190066004026101000a81548163ffffffff021916908363ffffffff1602179055508060ff167ff3c6ef0f972a2318778c473d9159a24462efb8565045b5882dbd9b8f96d848536001604051808260048015610a7c576020028201916000905b82829054906101000a900463ffffffff1663ffffffff1681526020019060040190602082600301049283019260010382029150808411610a3f5790505b505091505060405180910390a2505050565b3360009081526002602052604090205460609060ff161515610afa576040805160e560020a62461bcd02815260206004820152600f60248201527f4e6f20766f746520736f206661722e0000000000000000000000000000000000604482015290519081900360640190fd5b336000908152600260208181526040928390206001908101805485519281161561010002600019011693909304601f81018390048302820183019094528381529290830182828015610b8d5780601f10610b6257610100808354040283529160200191610b8d565b820191906000526020600020905b815481529060010190602001808311610b7057829003601f168201915b505050505090505b90565b600490565b60005474010000000000000000000000000000000000000000900464ffffffffff1681565b610bca610ce7565b1515610c0e576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611107833981519152604482015290519081900360640190fd5b60008054604051600160a060020a03909116917ff8df31144d9c2f0f6b59d69b8b98abd5459d07f2742c4df920b25aae33c6482091a26000805473ffffffffffffffffffffffffffffffffffffffff19169055565b3360009081526002602052604090205460ff1690565b610c81610ce7565b1515610cc5576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611107833981519152604482015290519081900360640190fd5b610ccd610cd8565b600160a060020a0316ff5b600054600160a060020a031690565b600054600160a060020a0316331490565b3360009081526002602052604081205460ff161515610d61576040805160e560020a62461bcd02815260206004820152600f60248201527f4e6f20766f746520736f206661722e0000000000000000000000000000000000604482015290519081900360640190fd5b5033600090815260026020526040902054610100900460ff1690565b6000610d87610b98565b60ff168260ff16101515610e0b576040805160e560020a62461bcd02815260206004820152603d60248201527f43686f696365206d757374206265206c657373207468616e20636f6e7472616360448201527f7420636f6e66696775726564206e756d6265724f6643686f696365732e000000606482015290519081900360840190fd5b600160ff831660048110610e1b57fe5b600891828204019190066004029054906101000a900463ffffffff169050919050565b610e466110cd565b60408051608081019182905290600190600490826000855b82829054906101000a900463ffffffff1663ffffffff1681526020019060040190602082600301049283019260010382029150808411610e5e5790505050505050905090565b610eac610ce7565b1515610ef0576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611107833981519152604482015290519081900360640190fd5b610ef981610f87565b50565b610f04610ce7565b1515610f48576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020611107833981519152604482015290519081900360640190fd5b80600160a060020a0316ff5b81810164ffffffffff8084169082161015610f6b57fe5b92915050565b81810163ffffffff8084169082161015610f6b57fe5b600160a060020a0381161515610fe7576040805160e560020a62461bcd02815260206004820152601860248201527f4e6577206f776e65722063616e6e6f74206265203078302e0000000000000000604482015290519081900360640190fd5b60008054604051600160a060020a03808516939216917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e091a36000805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061109057805160ff19168380011785556110bd565b828001600101855582156110bd579182015b828111156110bd5782518255916020019190600101906110a2565b506110c99291506110ec565b5090565b6080604051908101604052806004906020820280388339509192915050565b610b9591905b808211156110c957600081556001016110f256004f6e6c7920746865206f776e65722063616e20646f20746869732e0000000000a165627a7a7230582024c9370f307f7d02f807142975cbb888008a898d83aa2628e76471ba490d5e1e0029";

    public static final String FUNC_VOTERSINFO = "votersInfo";

    public static final String FUNC_RECOVERTOKENS = "recoverTokens";

    public static final String FUNC_CASTVOTE = "castVote";

    public static final String FUNC_THISVOTERSNAME = "thisVotersName";

    public static final String FUNC_NUMBEROFCHOICES = "numberOfChoices";

    public static final String FUNC_VOTECOUNTTOTAL = "voteCountTotal";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_THISVOTEREXISTS = "thisVoterExists";

    public static final String FUNC_DESTROY = "destroy";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_ISOWNER = "isOwner";

    public static final String FUNC_THISVOTERSCHOICE = "thisVotersChoice";

    public static final String FUNC_VOTESPERCHOICE = "votesPerChoice";

    public static final String FUNC_CURRENTRESULT = "currentResult";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_DESTROYANDSEND = "destroyAndSend";

    public static final Event NEWVOTE_EVENT = new Event("NewVote", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>(true) {}, new TypeReference<StaticArray4<Uint32>>() {}));
    ;

    public static final Event OWNERSHIPRENOUNCED_EVENT = new Event("OwnershipRenounced", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
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
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
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

    public RemoteCall<StaticArray4<Uint32>> currentResult() {
        final Function function = new Function(FUNC_CURRENTRESULT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<StaticArray4<Uint32>>() {}));
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

    public List<NewVoteEventResponse> getNewVoteEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(NEWVOTE_EVENT, transactionReceipt);
        ArrayList<NewVoteEventResponse> responses = new ArrayList<NewVoteEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            NewVoteEventResponse typedResponse = new NewVoteEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.addedVote = (Uint8) eventValues.getIndexedValues().get(0);
            typedResponse.allVotes = (StaticArray4<Uint32>) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<NewVoteEventResponse> newVoteEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, NewVoteEventResponse>() {
            @Override
            public NewVoteEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(NEWVOTE_EVENT, log);
                NewVoteEventResponse typedResponse = new NewVoteEventResponse();
                typedResponse.log = log;
                typedResponse.addedVote = (Uint8) eventValues.getIndexedValues().get(0);
                typedResponse.allVotes = (StaticArray4<Uint32>) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Observable<NewVoteEventResponse> newVoteEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(NEWVOTE_EVENT));
        return newVoteEventObservable(filter);
    }

    public List<OwnershipRenouncedEventResponse> getOwnershipRenouncedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPRENOUNCED_EVENT, transactionReceipt);
        ArrayList<OwnershipRenouncedEventResponse> responses = new ArrayList<OwnershipRenouncedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipRenouncedEventResponse typedResponse = new OwnershipRenouncedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OwnershipRenouncedEventResponse> ownershipRenouncedEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnershipRenouncedEventResponse>() {
            @Override
            public OwnershipRenouncedEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPRENOUNCED_EVENT, log);
                OwnershipRenouncedEventResponse typedResponse = new OwnershipRenouncedEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Observable<OwnershipRenouncedEventResponse> ownershipRenouncedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPRENOUNCED_EVENT));
        return ownershipRenouncedEventObservable(filter);
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
            typedResponse.newOwner = (Address) eventValues.getIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OwnershipTransferredEventResponse> ownershipTransferredEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnershipTransferredEventResponse>() {
            @Override
            public OwnershipTransferredEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
                typedResponse.newOwner = (Address) eventValues.getIndexedValues().get(1);
                return typedResponse;
            }
        });
    }

    public Observable<OwnershipTransferredEventResponse> ownershipTransferredEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventObservable(filter);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Voting.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Voting> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Voting.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Voting.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Voting> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Voting.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Voting(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Voting(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class NewVoteEventResponse {
        public Log log;

        public Uint8 addedVote;

        public StaticArray4<Uint32> allVotes;
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
