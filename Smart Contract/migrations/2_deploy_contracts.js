var Voting = artifacts.require("./Voting.sol");
var DummyERC20 = artifacts.require("./testhelper/DummyERC20.sol");

module.exports = function (deployer, network) {
    deployer.deploy(Voting, 4);

    // Only deploy Dummy Token during tests in develop network
    if (network === 'develop') {
        deployer.deploy(DummyERC20, 4);
    }
};

