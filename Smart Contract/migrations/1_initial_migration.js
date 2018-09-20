var Migrations = artifacts.require("./misc/Migrations.sol");

module.exports = function (deployer) {

    // don't deploy migrations on mainnet or ropsten
    if (deployer.network_id !== 1 && deployer.network_id !== 3) {
        deployer.deploy(Migrations);
    }

};
