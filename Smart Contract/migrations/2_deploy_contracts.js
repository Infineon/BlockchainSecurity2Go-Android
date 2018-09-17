var Voting = artifacts.require("./Voting.sol");
var DummyERC20 = artifacts.require("./testhelper/DummyERC20.sol");

module.exports = function (deployer) {

    // Deploy MAIN contract:
    deployer.deploy(Voting, 4);

    // Dont deploy dummy token on mainnet or ropsten (only used for tests)
    if (deployer.network_id !== 1 && deployer.network_id !== 3) {
        deployer.deploy(DummyERC20, 4);
    }
};

