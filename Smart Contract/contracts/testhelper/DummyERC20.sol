pragma solidity 0.4.24;

import "../ownership/Ownable.sol";
import "../interfaces/IERC20.sol";
import "./SafeMath.sol";


/**
 * This DUMMY ERC20 token is only used for testing, to check if
 * ERC-20 tokens can be rescued out of the contract.
 *
 * This is NO ERC-20 COMPLIANT implementation,
 * don't use for anything else than tests.
 */
contract DummyERC20 is Ownable, IERC20 {
    using SafeMath for uint256;

    mapping(address => uint256) internal balances;
    uint256 internal totalSupply_;

    /**
     * @dev Total number of tokens in existence
     */
    function totalSupply() public view returns (uint256) {
        return totalSupply_;
    }

    /**
     * @dev Transfer token for a specified address
     * @param _to The address to transfer to.
     * @param _value The amount to be transferred.
     */
    function transfer(address _to, uint256 _value)
    public
    returns (bool) {
        require(_value <= balances[msg.sender]);
        require(_to != address(0));

        balances[msg.sender] = balances[msg.sender].sub(_value);
        balances[_to] = balances[_to].add(_value);
        emit Transfer(msg.sender, _to, _value);
        return true;
    }

    /**
     * Allows to create tokens out of thin air, used for the tests.
     */
    function mint(address receiver, uint256 amount)
    public
    onlyOwner {
        balances[receiver] = balances[receiver].add(amount);
        totalSupply_ = totalSupply_.add(amount);
    }

    /**
     * @dev Gets the balance of the specified address.
     * @param _owner The address to query the the balance of.
     * @return An uint256 representing the amount owned by the passed address.
     */
    function balanceOf(address _owner)
    public
    view
    returns (uint256) {
        return balances[_owner];
    }

}


