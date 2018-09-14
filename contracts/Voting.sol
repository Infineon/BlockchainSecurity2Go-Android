pragma solidity ^0.4.4;

contract Voting {

    address owner;
    uint8 public maxAnswers;
    mapping(address => Voter) public voters;
    uint voterCount;
    uint8[] answersCount;


    struct Voter {
        string name;
        uint8 answer;
        bool exists;
    }

    event VoteHappened(
        address indexed voter,
        uint8 indexed answer,
        uint8 newAnswersCount
    );

    /* -------------------------------------------------------------------
    constructor
    ------------------------------------------------------------------- */
    constructor (uint8 initMaxAnswers) public {
        owner = msg.sender;
        maxAnswers = initMaxAnswers;
        answersCount = new uint8[](maxAnswers);
    }


    /* -------------------------------------------------------------------
    destructor - Owner has possibility to kill contract.
    ------------------------------------------------------------------- */
    function kill() public {
        require(msg.sender == owner, "Not the owner of the contract.");
        selfdestruct(owner);
    }


    /* ---------------------------
    giveVote - Add a new vote to voting.
    --------------------------- */
    function giveVote(string voterName, uint8 answer) public returns (bool){
        // answer must be given
        require(answer < maxAnswers, "argument answer must be less than contract configured maxAnswer");

        bytes memory voterNameBytes = bytes(voterName);
        //  voter name has to have at least 3 chars
        require(voterNameBytes.length > 2, "Name of voter has to less chars.");

        // duplicate key check
        require(!voters[msg.sender].exists, "Already voted.");

        // everything ok add voter
        voters[msg.sender] = Voter(voterName, answer, true);
        voterCount++;
        answersCount[answer]++;

        emit VoteHappened(msg.sender, answer, answersCount[answer]);

        return true;
    }


    /* -------------------------------------------------------------------
    getVotersAnswer - Return answer of voter if voter already voted or not.
    ------------------------------------------------------------------- */
    function getVotersAnswer() public view returns (uint8) {
        //check if voter exists in mapping
        require(voters[msg.sender].exists, "No vote so far.");

        return voters[msg.sender].answer;
    }

    /* -------------------------------------------------------------------
    getVotersName - Return name of voter if voter already voted or not.
    ------------------------------------------------------------------- */
    function getVotersName() public view returns (string) {
        //check if voter exists in mapping
        require(voters[msg.sender].exists, "No vote so far.");

        return voters[msg.sender].name;
    }

    /* -------------------------------------------------------------------
    getVotersName - Return name of voter if voter already voted or not.
    ------------------------------------------------------------------- */
    function getAnswerCounts() public view returns (uint8[]) {
        return answersCount;
    }
}

