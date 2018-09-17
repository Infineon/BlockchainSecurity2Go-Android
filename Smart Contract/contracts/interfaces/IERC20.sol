pragma solidity 0.4.24;


/**
 * @title ERC20 interface
 *
 * @notice Used to call methods in ERC-20 contracts.
 *
 * @dev see https://eips.ethereum.org/EIPS/eip-20
 */
interface IERC20 {

    function transfer(address to, uint256 value)
    external
    returns (bool);

    function balanceOf(address who)
    external
    view
    returns (uint256);

    function totalSupply()
    external
    view
    returns (uint256);

    event Transfer(
        address indexed from,
        address indexed to,
        uint256 value
    );

}
