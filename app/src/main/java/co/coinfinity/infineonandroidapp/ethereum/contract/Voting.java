package co.coinfinity.infineonandroidapp.ethereum.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.StaticArray4;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint40;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

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
    public static final String FUNC_RECOVERTOKENS = "recoverTokens";

    public static final String FUNC_CASTVOTE = "castVote";
    public static final String FUNC_NUMBEROFPOSSIBLECHOICES = "numberOfPossibleChoices";
    public static final String FUNC_SETWHITELIST = "setWhiteList";
    public static final String FUNC_WHITELISTEDSENDERADDRESSES = "whitelistedSenderAddresses";
    public static final String FUNC_RESETDEMO = "resetDemo";
    private static final String BINARY = "608060405260008054600160a060020a03191633179055610dee806100256000396000f3006080604052600436106100da5763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630fb524ce811461013957806316114acd1461014e57806328d5e1171461016f5780632b26fac41461019a57806331f2c8a5146101b1578063519be2f5146101fe5780635961e4fb14610213578063715018a61461024257806383197ef0146102575780638da5cb5b1461026c5780638f32d59b1461029d578063ab6f9e4a146102c6578063c6c4676c146102fa578063f2fde38b1461030f578063f5074f4114610330575b3480156100e657600080fd5b506040805160e560020a62461bcd02815260206004820181905260248201527f46616c6c6261636b2066756e6374696f6e20616c77617973207468726f77732e604482015290519081900360640190fd5b005b34801561014557600080fd5b50610137610351565b34801561015a57600080fd5b50610137600160a060020a036004351661056a565b34801561017b57600080fd5b50610184610776565b6040805160ff9092168252519081900360200190f35b3480156101a657600080fd5b50610137600461077c565b3480156101bd57600080fd5b506101c6610818565b6040518082608080838360005b838110156101eb5781810151838201526020016101d3565b5050505090500191505060405180910390f35b34801561020a57600080fd5b5061013761085d565b34801561021f57600080fd5b506102286108fb565b6040805164ffffffffff9092168252519081900360200190f35b34801561024e57600080fd5b50610137610908565b34801561026357600080fd5b506101376109a9565b34801561027857600080fd5b50610281610a08565b60408051600160a060020a039092168252519081900360200190f35b3480156102a957600080fd5b506102b2610a17565b604080519115158252519081900360200190f35b3480156102d257600080fd5b506102e160ff60043516610a28565b6040805163ffffffff9092168252519081900360200190f35b34801561030657600080fd5b506101c6610ade565b34801561031b57600080fd5b50610137600160a060020a0360043516610b44565b34801561033c57600080fd5b50610137600160a060020a0360043516610b9c565b60006001810154600160a060020a03163314156103705750600061043f565b6001800154600160a060020a031633141561038d5750600161043f565b600160020154600160a060020a03163314156103ab5750600261043f565b600160030154600160a060020a03163314156103c95750600361043f565b6040805160e560020a62461bcd02815260206004820152603160248201527f4f6e6c792077686974656c69737465642073656e64657220616464726573736560448201527f732063616e206361737420766f7465732e000000000000000000000000000000606482015290519081900360840190fd5b6005546104549064ffffffffff166001610bf4565b6005805464ffffffffff191664ffffffffff929092169190911790556104a6600660ff83166004811061048357fe5b600891828204019190066004029054906101000a900463ffffffff166001610c11565b600660ff8316600481106104b657fe5b600891828204019190066004026101000a81548163ffffffff021916908363ffffffff1602179055508060ff167ff3c6ef0f972a2318778c473d9159a24462efb8565045b5882dbd9b8f96d84853600660405180826004801561055a576020028201916000905b82829054906101000a900463ffffffff1663ffffffff168152602001906004019060208260030104928301926001038202915080841161051d5790505b505091505060405180910390a250565b6000610574610a17565b15156105b8576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020610da3833981519152604482015290519081900360640190fd5b604080517f70a082310000000000000000000000000000000000000000000000000000000081523060048201529051600160a060020a038416916370a082319160248083019260209291908290030181600087803b15801561061957600080fd5b505af115801561062d573d6000803e3d6000fd5b505050506040513d602081101561064357600080fd5b50519050600160a060020a03821663a9059cbb61065e610a08565b836040518363ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018083600160a060020a0316600160a060020a0316815260200182815260200192505050602060405180830381600087803b1580156106ca57600080fd5b505af11580156106de573d6000803e3d6000fd5b505050506040513d60208110156106f457600080fd5b50511515610772576040805160e560020a62461bcd02815260206004820152603160248201527f546f6b656e207472616e73666572206661696c65642c207472616e736665722860448201527f292072657475726e65642066616c73652e000000000000000000000000000000606482015290519081900360840190fd5b5050565b60045b90565b610784610a17565b15156107c8576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020610da3833981519152604482015290519081900360640190fd5b6107d56001826004610cef565b507f69beed04fe5b4341d5b8a8858815115b85b6e84eef37c2aa98f054327f58333f8160405180826004602002808284376040519201829003935090915050a150565b610820610d52565b6040805160808101918290529060019060049082845b8154600160a060020a03168152600190910190602001808311610836575050505050905090565b610865610a17565b15156108a9576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020610da3833981519152604482015290519081900360640190fd5b6005805464ffffffffff19169055600680546fffffffffffffffffffffffffffffffff191690556040517f30bfd50aaa5fd4e81c17f3a1165e69b24fbc4e244743b59c6c1efa7fb968160390600090a1565b60055464ffffffffff1681565b610910610a17565b1515610954576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020610da3833981519152604482015290519081900360640190fd5b60008054604051600160a060020a03909116917ff8df31144d9c2f0f6b59d69b8b98abd5459d07f2742c4df920b25aae33c6482091a26000805473ffffffffffffffffffffffffffffffffffffffff19169055565b6109b1610a17565b15156109f5576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020610da3833981519152604482015290519081900360640190fd5b6109fd610a08565b600160a060020a0316ff5b600054600160a060020a031690565b600054600160a060020a0316331490565b6000600460ff831610610aab576040805160e560020a62461bcd02815260206004820152602960248201527f43686f696365206d757374206265206c657373207468616e206e756d6265724f60448201527f6643686f696365732e0000000000000000000000000000000000000000000000606482015290519081900360840190fd5b600660ff831660048110610abb57fe5b600891828204019190066004029054906101000a900463ffffffff169050919050565b610ae6610d52565b60408051608081019182905290600690600490826000855b82829054906101000a900463ffffffff1663ffffffff1681526020019060040190602082600301049283019260010382029150808411610afe5790505050505050905090565b610b4c610a17565b1515610b90576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020610da3833981519152604482015290519081900360640190fd5b610b9981610c27565b50565b610ba4610a17565b1515610be8576040805160e560020a62461bcd02815260206004820152601b6024820152600080516020610da3833981519152604482015290519081900360640190fd5b80600160a060020a0316ff5b81810164ffffffffff8084169082161015610c0b57fe5b92915050565b81810163ffffffff8084169082161015610c0b57fe5b600160a060020a0381161515610c87576040805160e560020a62461bcd02815260206004820152601860248201527f4e6577206f776e65722063616e6e6f74206265203078302e0000000000000000604482015290519081900360640190fd5b60008054604051600160a060020a03808516939216917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e091a36000805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055565b8260048101928215610d42579160200282015b82811115610d4257815473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03843516178255602090920191600190910190610d02565b50610d4e929150610d71565b5090565b6080604051908101604052806004906020820280388339509192915050565b61077991905b80821115610d4e57805473ffffffffffffffffffffffffffffffffffffffff19168155600101610d7756004f6e6c7920746865206f776e65722063616e20646f20746869732e0000000000a165627a7a72305820aa2bcde8ac49b7f82457076cb36c72732e8bcc7150c2f2d387bccb26c3e45b320029";

    public static final String FUNC_VOTECOUNTTOTAL = "voteCountTotal";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_DESTROY = "destroy";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_ISOWNER = "isOwner";

    public static final String FUNC_VOTESPERCHOICE = "votesPerChoice";

    public static final String FUNC_CURRENTRESULT = "currentResult";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_DESTROYANDSEND = "destroyAndSend";

//    public static final Event NEWVOTE_EVENT = new Event("NewVote",
//            Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>(true) {}, new TypeReference<StaticArray4<Uint32>>() {}));
//    ;
//
//    public static final Event WHITELISTUPDATED_EVENT = new Event("WhitelistUpdated",
//            Arrays.<TypeReference<?>>asList(new TypeReference<StaticArray4<Address>>() {}));
//    ;
//
//    public static final Event DEMORESETTED_EVENT = new Event("DemoResetted",
//            Arrays.<TypeReference<?>>asList());
//    ;
//
//    public static final Event OWNERSHIPRENOUNCED_EVENT = new Event("OwnershipRenounced",
//            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
//    ;
//
//    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred",
//            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
//    ;

    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Voting.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<Voting> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Voting.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public RemoteCall<TransactionReceipt> castVote() {
        final Function function = new Function(
                FUNC_CASTVOTE,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> recoverTokens(Address token) {
        final Function function = new Function(
                FUNC_RECOVERTOKENS,
                Arrays.<Type>asList(token),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Uint8> numberOfPossibleChoices() {
        final Function function = new Function(FUNC_NUMBEROFPOSSIBLECHOICES,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> setWhiteList(StaticArray4<Address> whitelistedSenders) {
        final Function function = new Function(
                FUNC_SETWHITELIST,
                Arrays.<Type>asList(whitelistedSenders),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<StaticArray4<Address>> whitelistedSenderAddresses() {
        final Function function = new Function(FUNC_WHITELISTEDSENDERADDRESSES,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<StaticArray4<Address>>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> resetDemo() {
        final Function function = new Function(
                FUNC_RESETDEMO,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Uint40> voteCountTotal() {
        final Function function = new Function(FUNC_VOTECOUNTTOTAL,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint40>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
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
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Bool> isOwner() {
        final Function function = new Function(FUNC_ISOWNER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Uint32> votesPerChoice(Uint8 option) {
        final Function function = new Function(FUNC_VOTESPERCHOICE,
                Arrays.<Type>asList(option),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint32>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<StaticArray4<Uint32>> currentResult() {
        final Function function = new Function(FUNC_CURRENTRESULT,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<StaticArray4<Uint32>>() {
                }));
        return executeRemoteCallSingleValueReturn(function);
    }

//    public List<NewVoteEventResponse> getNewVoteEvents(TransactionReceipt transactionReceipt) {
//        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(NEWVOTE_EVENT, transactionReceipt);
//        ArrayList<NewVoteEventResponse> responses = new ArrayList<NewVoteEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            NewVoteEventResponse typedResponse = new NewVoteEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.addedVote = (Uint8) eventValues.getIndexedValues().get(0);
//            typedResponse.allVotes = (StaticArray4<Uint32>) eventValues.getNonIndexedValues().get(0);
//            responses.add(typedResponse);
//        }
//        return responses;
//    }
//
//    public Observable<NewVoteEventResponse> newVoteEventObservable(EthFilter filter) {
//        return web3j.ethLogObservable(filter).map(new Func1<Log, NewVoteEventResponse>() {
//            @Override
//            public NewVoteEventResponse call(Log log) {
//                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(NEWVOTE_EVENT, log);
//                NewVoteEventResponse typedResponse = new NewVoteEventResponse();
//                typedResponse.log = log;
//                typedResponse.addedVote = (Uint8) eventValues.getIndexedValues().get(0);
//                typedResponse.allVotes = (StaticArray4<Uint32>) eventValues.getNonIndexedValues().get(0);
//                return typedResponse;
//            }
//        });
//    }
//
//    public Observable<NewVoteEventResponse> newVoteEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
//        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
//        filter.addSingleTopic(EventEncoder.encode(NEWVOTE_EVENT));
//        return newVoteEventObservable(filter);
//    }
//
//    public List<WhitelistUpdatedEventResponse> getWhitelistUpdatedEvents(TransactionReceipt transactionReceipt) {
//        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(WHITELISTUPDATED_EVENT, transactionReceipt);
//        ArrayList<WhitelistUpdatedEventResponse> responses = new ArrayList<WhitelistUpdatedEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            WhitelistUpdatedEventResponse typedResponse = new WhitelistUpdatedEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.whitelistedSenderAdresses = (StaticArray4<Address>) eventValues.getNonIndexedValues().get(0);
//            responses.add(typedResponse);
//        }
//        return responses;
//    }
//
//    public Observable<WhitelistUpdatedEventResponse> whitelistUpdatedEventObservable(EthFilter filter) {
//        return web3j.ethLogObservable(filter).map(new Func1<Log, WhitelistUpdatedEventResponse>() {
//            @Override
//            public WhitelistUpdatedEventResponse call(Log log) {
//                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(WHITELISTUPDATED_EVENT, log);
//                WhitelistUpdatedEventResponse typedResponse = new WhitelistUpdatedEventResponse();
//                typedResponse.log = log;
//                typedResponse.whitelistedSenderAdresses = (StaticArray4<Address>) eventValues.getNonIndexedValues().get(0);
//                return typedResponse;
//            }
//        });
//    }
//
//    public Observable<WhitelistUpdatedEventResponse> whitelistUpdatedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
//        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
//        filter.addSingleTopic(EventEncoder.encode(WHITELISTUPDATED_EVENT));
//        return whitelistUpdatedEventObservable(filter);
//    }
//
//    public List<DemoResettedEventResponse> getDemoResettedEvents(TransactionReceipt transactionReceipt) {
//        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DEMORESETTED_EVENT, transactionReceipt);
//        ArrayList<DemoResettedEventResponse> responses = new ArrayList<DemoResettedEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            DemoResettedEventResponse typedResponse = new DemoResettedEventResponse();
//            typedResponse.log = eventValues.getLog();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }
//
//    public Observable<DemoResettedEventResponse> demoResettedEventObservable(EthFilter filter) {
//        return web3j.ethLogObservable(filter).map(new Func1<Log, DemoResettedEventResponse>() {
//            @Override
//            public DemoResettedEventResponse call(Log log) {
//                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DEMORESETTED_EVENT, log);
//                DemoResettedEventResponse typedResponse = new DemoResettedEventResponse();
//                typedResponse.log = log;
//                return typedResponse;
//            }
//        });
//    }
//
//    public Observable<DemoResettedEventResponse> demoResettedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
//        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
//        filter.addSingleTopic(EventEncoder.encode(DEMORESETTED_EVENT));
//        return demoResettedEventObservable(filter);
//    }
//
//    public List<OwnershipRenouncedEventResponse> getOwnershipRenouncedEvents(TransactionReceipt transactionReceipt) {
//        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPRENOUNCED_EVENT, transactionReceipt);
//        ArrayList<OwnershipRenouncedEventResponse> responses = new ArrayList<OwnershipRenouncedEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            OwnershipRenouncedEventResponse typedResponse = new OwnershipRenouncedEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
//            responses.add(typedResponse);
//        }
//        return responses;
//    }
//
//    public Observable<OwnershipRenouncedEventResponse> ownershipRenouncedEventObservable(EthFilter filter) {
//        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnershipRenouncedEventResponse>() {
//            @Override
//            public OwnershipRenouncedEventResponse call(Log log) {
//                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPRENOUNCED_EVENT, log);
//                OwnershipRenouncedEventResponse typedResponse = new OwnershipRenouncedEventResponse();
//                typedResponse.log = log;
//                typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
//                return typedResponse;
//            }
//        });
//    }
//
//    public Observable<OwnershipRenouncedEventResponse> ownershipRenouncedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
//        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
//        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPRENOUNCED_EVENT));
//        return ownershipRenouncedEventObservable(filter);
//    }
//
//    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
//        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
//        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
//            typedResponse.newOwner = (Address) eventValues.getIndexedValues().get(1);
//            responses.add(typedResponse);
//        }
//        return responses;
//    }
//
//    public Observable<OwnershipTransferredEventResponse> ownershipTransferredEventObservable(EthFilter filter) {
//        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnershipTransferredEventResponse>() {
//            @Override
//            public OwnershipTransferredEventResponse call(Log log) {
//                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
//                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
//                typedResponse.log = log;
//                typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
//                typedResponse.newOwner = (Address) eventValues.getIndexedValues().get(1);
//                return typedResponse;
//            }
//        });
//    }
//
//    public Observable<OwnershipTransferredEventResponse> ownershipTransferredEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
//        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
//        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
//        return ownershipTransferredEventObservable(filter);
//    }

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

    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class NewVoteEventResponse {
        public Log log;

        public Uint8 addedVote;

        public StaticArray4<Uint32> allVotes;
    }

    public static class WhitelistUpdatedEventResponse {
        public Log log;

        public StaticArray4<Address> whitelistedSenderAdresses;
    }

    public static class DemoResettedEventResponse {
        public Log log;
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
