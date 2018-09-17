// We use chai for writing the test-suites
require('chai')
    .use(require('chai-as-promised'))
    .use(require('bn-chai')(web3.utils.BN))
    .should();

// Import contract artifacts:
const Voting = artifacts.require('./Voting.sol');

// import some helper functions:
const {expectFail, matchLogs, nulladdress, getBalance, getTime, travelIntoTheFuture} = require('./testutils');




contract('Voting', function (accounts) {

        // define some accounts and give them readable names
        const [owner, voter1, voter2, voter3, otherAcc1, otherAcc2, otherAcc3] = accounts;

        it('should have 4 possible choices', async function () {
            let voting = await Voting.deployed();
            // 4 is provided as constructior argument in the migration script ("2_deploy_contracts.js")
            (await voting.numberOfPossibleChoices()).should.eq.BN(4);
        });

        // it('should have 0 votes after deployment', async function () {
        //     let voting = await Voting.deployed();
        //     voting.get
        // });
    }
);