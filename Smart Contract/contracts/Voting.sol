pragma solidity 0.4.24;

import "./ownership/Ownable.sol";
import "./lifecycle/Destructible.sol";
import "./tokenutils/CanRescueERC20.sol";


/**
 * @title Simple Voting/Poll Demo for Infineon for TRUST TECH 2018
 * (https://www.trustech-event.com/)
 *
 * This is just a DEMO! It contains a reset function and makes
 * other assumptions which only make sense in the context of a demo.
 *
 * Don't use it like this in a production setup!
 *
 * @author Coinfinity (www.coinfinity.co), 2018
 */
contract Voting is Ownable, Destructible, CanRescueERC20 {

    /**
     * @dev number of possible choices. Constant set at compile time.
     *     (Note: if this is changed you also have to adapt the
     *     "castVote" function!)
     */
    uint8 internal constant NUMBER_OF_CHOICES = 4;

    /**
     * @notice Only these adresses are allowed to send votes. Depending
     *     on the sending address the voter's choice is dermined.
     *     (i.e.: if sending from allowedSenderAdresses[0] means vote
     *     for choice 0.)
     */
    address[NUMBER_OF_CHOICES] internal whitelistedSenderAdresses;

    /**
     * @notice Number of total cast votes (uint40 is enough as at most
     *     we support 2**8 choices and 2**32 votes per choice).
     */
    uint40 public voteCountTotal;

    /**
     * @notice Number of votes, summarized per choice.
     *
     * @dev uint32 allows 4,294,967,296 possible votes per choice, should be enough,
     *     and still allows 8 entries to be packed in a single storage slot
     *     (EVM wordsize is 256 bit). And of course we check for overflows.
     */
    uint32[NUMBER_OF_CHOICES] internal currentVoteResults;

    /**
     * @notice Event gets emitted every time when a new vote is cast.
     *
     * @param addedVote choice in the vote
     * @param allVotes array containing updated intermediate result
     */
    event NewVote(uint8 indexed addedVote, uint32[NUMBER_OF_CHOICES] allVotes);

    /**
     * @notice Event gets emitted every time the whitelisted sender addresses
     *     get updated.
     */
    event WhitelistUpdated(address[NUMBER_OF_CHOICES] whitelistedSenderAdresses);

    /**
     * @notice Event gets emitted every time this demo contract gets resetted.
     */
    event DemoResetted();

    /**
     * @notice Fallback function. We do not allow to be ether sent to us. And we also
     * do not allow transactions without any function call. Fallback function
     * simply always throws.
     */
    function()
    public {
        require(false, "Fallback function always throws.");
    }

    /**
     * @notice Only the owner can define which addresses are allowed to vote
     *     (and also which address stands for which vote choice)
     *
     * @param whitelistedSenders array of allowed vote sending addresses,
     *     address at index 0 will vote for choice 0, address at index 1
     *     will vote for choice 1, etc.
     */
    function setWhiteList(address[NUMBER_OF_CHOICES] whitelistedSenders)
    external
    onlyOwner {
        // Assumption: we assume that owner takes care that list contains no duplicates.
        // No duplicate check in here.
        whitelistedSenderAdresses = whitelistedSenders;
        emit WhitelistUpdated(whitelistedSenders);
    }

    /**
     * @notice As this is just a DEMO contract, allow the onwer to reset the
     *     state of the Demo conract.
     */
    function resetDemo()
    external
    onlyOwner {
        voteCountTotal = 0;
        currentVoteResults[0] = 0;
        currentVoteResults[1] = 0;
        currentVoteResults[2] = 0;
        currentVoteResults[3] = 0;
        emit DemoResetted();
    }

    /**
     * @notice Cast your note. The sending address determines the choice you
     *      are voting for (each choice has its own sending address). For the Demo
     *      there will be 1 Infineon card lying around for each choice, and the
     *      visitor chooses by using a specific card to to send the vote transaction.
     */
    function castVote()
    external {
        uint8 choice;
        if (msg.sender == whitelistedSenderAdresses[0]) {
            choice = 0;
        } else if (msg.sender == whitelistedSenderAdresses[1]) {
            choice = 1;
        } else if (msg.sender == whitelistedSenderAdresses[2]) {
            choice = 2;
        } else if (msg.sender == whitelistedSenderAdresses[3]) {
            choice = 3;
        } else {
            require(false, "Only whitelisted sender addresses can cast votes.");
        }

        // everything ok, add voter
        voteCountTotal = safeAdd40(voteCountTotal, 1);
        currentVoteResults[choice] = safeAdd32(currentVoteResults[choice], 1);

        // emit a NewVote event at this point in time, so that a web3 Dapp
        // can react it to it immediately. Emit full current vote state, as
        // events are cheaper for light clients than querying the state.
        emit NewVote(choice, currentVoteResults);
    }

    /**
     * @notice Return array with sums of votes per choice.
     */
    function currentResult()
    external
    view
    returns (uint32[NUMBER_OF_CHOICES]) {
        return currentVoteResults;
    }

    /**
     * @notice Return array of allowed voter addresses. Address at index 0
     *     represents votes for choice 0, addresses at index 1 represent
     *     votes for choice 1, etc.
     */
    function whitelistedSenderAddresses()
    external
    view
    returns (address[NUMBER_OF_CHOICES]) {
        return whitelistedSenderAdresses;
    }

    /**
     * @notice Return number of votes for one of the options.
     */
    function votesPerChoice(uint8 option)
    external
    view
    returns (uint32) {
        require(option < NUMBER_OF_CHOICES, "Choice must be less than numberOfChoices.");
        return currentVoteResults[option];
    }

    /**
     * @notice Returns the number of possible choices, which can be voted for.
     */
    function numberOfPossibleChoices()
    public
    pure
    returns (uint8) {
        return NUMBER_OF_CHOICES;
    }

    /**
     * @dev Adds two uint40 numbers, throws on overflow.
     */
    function safeAdd40(uint40 _a, uint40 _b)
    internal
    pure
    returns (uint40 c) {
        c = _a + _b;
        assert(c >= _a);
        return c;
    }

    /**
     * @dev Adds two uint32 numbers, throws on overflow.
     */
    function safeAdd32(uint32 _a, uint32 _b)
    internal
    pure
    returns (uint32 c) {
        c = _a + _b;
        assert(c >= _a);
        return c;
    }
}
