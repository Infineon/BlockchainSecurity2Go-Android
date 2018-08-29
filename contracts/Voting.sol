pragma solidity ^0.4.4;

contract Voting {

    address owner;
    int8 public maxAnswers;
    mapping(address => Voter) public voters;
    int voterCount;


    struct Voter {
        string name;
        int8 answer;
        bool exists;
    }

    /* -------------------------------------------------------------------
    constructor
    ------------------------------------------------------------------- */
    constructor (int8 initMaxAnswers) public {
        owner = msg.sender;
        maxAnswers = initMaxAnswers;
    }


    /* -------------------------------------------------------------------
    destructor - Owner has possibility to kill contract.
    ------------------------------------------------------------------- */
    function kill() public {
        require(msg.sender == owner);
        selfdestruct(owner);
    }


    /* ---------------------------
    giveVote - Add a new vote to voting.
    --------------------------- */
    function giveVote(string voterName, int8 answer) public returns (bool){
        // answer must be given
        require(answer < maxAnswers);

        bytes memory voterNameBytes = bytes(voterName);
        //  voter name has to have at least 3 chars
        require(voterNameBytes.length > 2, "Name of voter has to less chars.");

        // duplicate key check
        require(!voters[msg.sender].exists);

        // everything ok add voter
        voters[msg.sender] = Voter(voterName, answer, true);

        voterCount++;
        return true;
    }


    /* -------------------------------------------------------------------
    getVote - Return answer of voter if voter already voted or not.
    ------------------------------------------------------------------- */
    function getVote() public returns (int8) {
        //check if voter exists in mapping
        require(voters[msg.sender].exists);

        return voters[msg.sender].answer;
    }
}

