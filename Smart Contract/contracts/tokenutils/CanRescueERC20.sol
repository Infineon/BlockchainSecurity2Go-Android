pragma solidity 0.4.24;

import "../ownership/Ownable.sol";
import "../interfaces/IERC20.sol";


/**
 * @title CanRescueERC20
 *
 * Provides a function to recover ERC-20 tokens which are accidentally sent
 * to the address of this contract (the owner can rescue ERC-20 tokens sent
 * to this contract back to himself).
 */
contract CanRescueERC20 is Ownable {

    /**
     * Enable the owner to rescue ERC20 tokens, which are sent accidentally
     * to this contract.
     *
     * @dev This will be invoked by the owner, when owner wants to rescue tokens
     * @notice Recover tokens accidentally sent to this contract. They will be sent to the
     *     contract owner. Can only be called by the owner.
     * @param token Token which will we rescue to the owner from the contract
     */
    function recoverTokens(IERC20 token)
    public
    onlyOwner {
        uint256 balance = token.balanceOf(this);
        // Caution: ERC-20 standard doesn't require to throw exception on failures
        // (although most ERC-20 tokens do so), but instead returns a bool value.
        // Therefore let's check if it really returned true, and throw otherwise.
        require(token.transfer(owner(), balance), "Token transfer failed, transfer() returned false.");
    }

}
