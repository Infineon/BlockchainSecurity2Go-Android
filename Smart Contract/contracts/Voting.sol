pragma solidity 0.4.24;

import "./ownership/Ownable.sol";
import "./lifecycle/Destructible.sol";
import "./tokenutils/CanRescueERC20.sol";


/**
 * @title Simple Public Voting/Poll Demo
 *
 * @author Coinfinity (www.coinfinity.co), 2018
 */
contract Voting is Ownable, Destructible, CanRescueERC20 {

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
    uint32[] internal currentVoteResults;

    /**
     * @notice Mapping of address to vote details
     */
    mapping(address => Voter) public votersInfo;

    /**
     * @notice Event gets emitted every time when a new vote is cast.
     *
     * @param addedVote choice in the vote
     * @param allVotes array containing updated intermediate result
     */
    event NewVote(uint8 indexed addedVote, uint32[] allVotes);

    /**
     * @dev Represent info about a single voter.
     */
    struct Voter {
        bool exists;
        uint8 choice;
        string name;
    }

    /**
     * @dev Constructor
     */
    constructor (uint8 initMaxChoices)
    public {
        require(initMaxChoices >= 2, "Minimum 2 choices allowed.");
        // to avoid uint8 overflow:
        require(initMaxChoices <= 255, "Maximum 255 choices allowed.");

        // Initialize array:
        currentVoteResults.length = initMaxChoices;
        // this has the same effect as:
        // > currentVoteResults = new uint32[](initMaxChoices)"
        // but saves an SSTORE. In both cases the variable is layouted
        // as a "dynamically sized" array, as for constant sized array
        // layout the size would have to be a literal or a constant
        // (and solidity doesn't support yet constants to be set in
        // the constructor) but with "new" operator solidity immediately
        // writes 0 at position 0 (storage is always 0 before 1st write,
        // so this is unnecessary).
    }

    /**
     * Fallback function. We do not allow to be ether sent to us. And we also
     * do not allow transactions without any function call. Fallback function
     * simply always throws.
     */
    function()
    public {
        require(false, "Fallback function always throws.");
    }

    /**
     * @notice Cast your note. Each address can only vote once.
     * @param voterName Name of the voter, will be publicly visible on the blockchain
     * @param givenVote choice the caller has voted for
     */
    function castVote(string voterName, uint8 givenVote)
    external {
        // answer must be given
        require(givenVote < numberOfChoices(), "Choice must be less than contract configured numberOfChoices.");

        // check if already voted
        require(!votersInfo[msg.sender].exists, "This address has already voted. Vote denied.");

        //  voter name has to have at least 3 bytes (note: with utf8 some chars have
        // more than 1 byte, so this check is not fully accurate but ok here)
        require(bytes(voterName).length > 2, "Name of voter is too short.");

        // everything ok, add voter
        votersInfo[msg.sender] = Voter(true, givenVote, voterName);
        voteCountTotal = safeAdd40(voteCountTotal, 1);
        currentVoteResults[givenVote] = safeAdd32(currentVoteResults[givenVote], 1);

        // emit a NewVote event at this point in time, so that a web3 Dapp
        // can react it to it immediately. Emit full current vote state, as
        // events are cheaper for light clients than querying the state.
        emit NewVote(givenVote, currentVoteResults);
    }


    /**
    * @notice checks if this address has already cast a vote
    *  this is required to find out if it is safe to call the other "thisVoters..." views.
    */
    function thisVoterExists()
    external
    view
    returns (bool) {
        return votersInfo[msg.sender].exists;
    }

    /**
     * @notice Returns the vote details of calling address or throws
     *    if address has not voted yet.
     */
    function thisVotersChoice()
    external
    view
    returns (uint8) {
        // check if msg sender exists in voter mapping
        require(votersInfo[msg.sender].exists, "No vote so far.");
        return votersInfo[msg.sender].choice;
    }

    /**
     * @notice Returns the entered voter name of the calling address or throws
     *    if address has not voted yet.
     */
    function thisVotersName()
    external
    view
    returns (string) {
        // check if msg sender exists in voter mapping
        require(votersInfo[msg.sender].exists, "No vote so far.");
        return votersInfo[msg.sender].name;
    }



    /**
     * @notice Return array with sums of votes per choice.
     *
     * @dev Note that this only will work for external callers, and not
     *      for other contracts (as of solidity 0.4.25 returning of dynamically
     *      sized data is still not in stable, it's only available with the
     *      experimental "ABIEncoderV2" pragma). Also some block-explorers,
     *      like etherscan, will have problems to display this correctly.
     */
    function currentResult()
    external
    view
    returns (uint32[]) {
        return currentVoteResults;
    }

    /**
     * @notice Return number of votes for one of the options.
     */
    function votesPerChoice(uint8 option)
    external
    view
    returns (uint32) {
        require(option < numberOfChoices(), "Choice must be less than contract configured numberOfChoices.");
        return currentVoteResults[option];
    }

    /**
     * @notice Returns the number of possible choices, which can be voted for.
     */
    function numberOfChoices()
    public
    view
    returns (uint8) {
        // save as we only initialize array length in constructor
        // and there we check it's never larger than uint8.
        return uint8(currentVoteResults.length);
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
