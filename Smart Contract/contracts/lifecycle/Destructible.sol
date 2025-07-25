pragma solidity 0.4.24;


import "../ownership/Ownable.sol";


/**
 * @title Destructible
 * @dev Base contract that can be destroyed by owner. All funds in contract will be sent to the owner.
 */
contract Destructible is Ownable {

    /**
     * @notice Destructs this contract (removes it from the blockchain) and sends all funds in it
     *     to the owner.
     *
     * @dev Transfers the current balance to the owner and terminates the contract.
     */
    function destroy()
    public
    onlyOwner {
        selfdestruct(owner());
    }

    /**
     * @notice Destructs this contract (removes it from the blockchain) and sends all funds in it
     *     to the specified recipient address.
     *
     * @dev Transfers the current balance to the specified recipient and terminates the contract.
     */
    function destroyAndSend(address _recipient)
    public
    onlyOwner {
        selfdestruct(_recipient);
    }
}
