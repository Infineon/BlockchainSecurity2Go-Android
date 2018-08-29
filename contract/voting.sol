pragma solidity ^0.4.4;

contract voting
{
    address owner;

    int8    public answer_options;
    Voterstruct[] public voters;

    struct Voterstruct
    {
        address eth_address;
        string name;
        int8 answer;
    }

    /* -------------------------------------------------------------------
    constructor
    ------------------------------------------------------------------- */
    constructor (int8 init_answer_options) public {
        owner = msg.sender;
        answer_options = init_answer_options;
    } //// constructor


    /* -------------------------------------------------------------------
    destructor - Owner has possibility to kill contract.
    ------------------------------------------------------------------- */
    function kill() public {
        if (msg.sender == owner)
            selfdestruct(owner);
    } //// function kill()


    /* ---------------------------
    give_vote - Add a new vote to voting.
    --------------------------- */
    function give_vote(string voter_name, int8 answer) public returns (bool){
        /* answer not given*/
        if (answer > answer_options) return false;

        bytes memory _voter_name = bytes(voter_name);
        /*  voter name has to have at least 3 chars */
        if (_voter_name.length < 3) return false;

        /* everything ok add voter to array */
        voters.push(Voterstruct(msg.sender, voter_name, answer));

        return true;
    } //// give_vote


    /* -------------------------------------------------------------------
    get_status - Return if voter already voted or not.
    ------------------------------------------------------------------- */
    function get_status() public constant returns (bool)
    {
        uint arrayLength = voters.length;

        for (uint i = 0; i < arrayLength; i++) {
            if (voters[i].eth_address == msg.sender) return true;
        }

        return false;
    } //// get_status


} //// contract voting

