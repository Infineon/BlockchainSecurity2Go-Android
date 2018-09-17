// We use chai for writing the test-suites
require('chai')
    .use(require('chai-as-promised'))
    .use(require('bn-chai')(web3.utils.BN))
    .should();

// Import contract artifacts:
const Voting = artifacts.require('./Voting.sol');

// import some helper functions:
const {expectFailWithoutMsg, expectFail, checkEvents, nulladdress, getBalance, getTime, travelIntoTheFuture} = require('./testutils');

// Some error messages we expect to be thrown from the contract.
const errors = {
    onlyOwner: 'Only the owner can do this.',
    alreadyVoted: 'This address has already voted. Vote denied.',
    invalidChoice: 'Choice must be less than contract configured numberOfChoices.',
    nameTooShort: 'Name of voter is too short.',
    ownerZeroNotAllowed: 'New owner cannot be 0x0.'

};


contract('Vote: init checks', function (accounts) {

        // define some accounts and give them readable names
        const [owner, voter1, voter2, voter3, otherAcc1, otherAcc2, otherAcc3] = accounts;

        it('should have 4 possible choices', async function () {
            let v = await Voting.deployed();
            // 4 is provided as constructor argument in the migration script ("2_deploy_contracts.js")
            (await v.numberOfPossibleChoices()).should.eq.BN(4);
        });

        it('should have 0 votes after deployment', async function () {
            let v = await Voting.deployed();
            let totalCount = await v.voteCount();
            totalCount.should.be.eq.BN(0);
        });

        it('should return 4 entries in getCurrentResult', async function () {
            let v = await Voting.deployed();
            let currentResultArray = await v.getCurrentResult();
            currentResultArray.length.should.be.eq.BN(4);
        });

        it('should have 0 in all 4 entries in getCurrentResult', async function () {
            let v = await Voting.deployed();
            let currentResultArray = await v.getCurrentResult();
            currentResultArray[0].should.be.eq.BN(0);
            currentResultArray[1].should.be.eq.BN(0);
            currentResultArray[2].should.be.eq.BN(0);
            currentResultArray[3].should.be.eq.BN(0);
        });

        it('should have 0 in all 4 choice counters (queried separaately)', async function () {
            let v = await Voting.deployed();
            let singleVoteCount = await v.getVoteCountForOption(0);
            singleVoteCount.should.be.eq.BN(0);
            singleVoteCount = await v.getVoteCountForOption(1);
            singleVoteCount.should.be.eq.BN(0);
            singleVoteCount = await v.getVoteCountForOption(2);
            singleVoteCount.should.be.eq.BN(0);
            singleVoteCount = await v.getVoteCountForOption(3);
            singleVoteCount.should.be.eq.BN(0);
        });

        it('should return error if asking for vote counter of non-existing choice', async function () {
            let v = await Voting.deployed();
            await expectFailWithoutMsg(v.getVoteCountForOption(4));
        });

        it('should have no vote for a specific voter yet', async function () {
            let v = await Voting.deployed();
            let vote = await v.voters(voter1);
            vote.exists.should.be.equal(false);
            vote = await v.voters(voter2);
            vote.exists.should.be.equal(false);
        });

        it('should not accept ether sent to it', async function () {
            let v = await Voting.deployed();
            // send 1 ETH
            expectFailWithoutMsg(v.send(web3.utils.toWei('1'), {from: owner}));
            // send 1 wei
            expectFailWithoutMsg(v.send(1, {from: otherAcc1}));
        });
    }
);


contract('Vote: access checks', function (accounts) {

        // define some accounts and give them readable names
        const [owner, voter1, voter2, voter3, otherAcc1, otherAcc2, otherAcc3] = accounts;

        it('should have correct owner set', async function () {
            let v = await Voting.deployed();
            let returnedOwner = await v.owner();
            returnedOwner.should.be.equal(owner);
            returnedOwner.should.not.be.equal(otherAcc1);
            let isOwner = await v.isOwner({from: owner});
            isOwner.should.be.equal(true);
            isOwner = await v.isOwner({from: otherAcc1});
            isOwner.should.be.equal(false);
        });

        it('should not allow to be destructed by non-owner', async function () {
            let v = await Voting.deployed();
            await expectFail(v.destroy({from: otherAcc1}), errors.onlyOwner);
            await expectFail(v.destroyAndSend(otherAcc1, {from: otherAcc1}), errors.onlyOwner);
            await expectFail(v.destroyAndSend(otherAcc2, {from: otherAcc3}), errors.onlyOwner);
        });

        it('should not allow to rescue ERC-20 by non-owner', async function () {
            let v = await Voting.deployed();
            await expectFail(v.recoverTokens(otherAcc3, {from: otherAcc1}), errors.onlyOwner);
        });

        it('should allow to transfer ownership', async function () {
            let v = await Voting.deployed();
            await expectFail(v.transferOwnership(otherAcc3, {from: otherAcc1}), errors.onlyOwner);
            var {logs} = await v.transferOwnership(otherAcc1, {from: owner});
            checkEvents(logs, [{
                event: 'OwnershipTransferred',
                args: {
                    previousOwner: owner,
                    newOwner: otherAcc1
                }
            }]);
            var {logs} = await v.transferOwnership(owner, {from: otherAcc1});
            checkEvents(logs, [{
                event: 'OwnershipTransferred',
                args: {
                    previousOwner: otherAcc1,
                    newOwner: owner
                }
            }]);

        });

        it('should allow to transfer ownership from owner to itself', async function () {
            let v = await Voting.deployed();
            let {logs} = await v.transferOwnership(owner, {from: owner});
            checkEvents(logs, [{
                event: 'OwnershipTransferred',
                args: {
                    previousOwner: owner,
                    newOwner: owner
                }
            }]);
        });

        it('should not allow to transfer ownership from owner to 0x000....000', async function () {
            let v = await Voting.deployed();
            await expectFail(v.transferOwnership(nulladdress, {from: owner}), errors.ownerZeroNotAllowed);
        });

        it('should be allow to be destructed by owner', async function () {
            let v = await Voting.deployed();
            // works before
            let returnedOwner = await v.owner();
            returnedOwner.should.be.equal(owner);
            // destroy
            await v.destroy({from: owner});
            // should not work afterwards
            await v.owner().should.be.rejectedWith('is not a contract address');
        });

    }
);


contract('Vote: voting tests', function (accounts) {

        // define some accounts and give them readable names
        const [owner, voter1, voter2, voter3, voter4, voter5, voter6] = accounts;

        /**
         * Small helper functiom that returns the 4 digits as a BN.js array (like web3 returns it)
         */
        const arrayAsBn = function (a, b, c, d) {
            return [web3.utils.toBN(a), web3.utils.toBN(b), web3.utils.toBN(c), web3.utils.toBN(d)];
        };

        const compareResult = function (results, a, b, c, d) {
            results.length.should.be.equal(4);
            results[0].should.be.eq.BN(a);
            results[1].should.be.eq.BN(b);
            results[2].should.be.eq.BN(c);
            results[3].should.be.eq.BN(d);
        };

        /**
         * Small helper that checks if all counters in tghe contract have the values we expect
         */
        const checkResultsInContract = async function (voteContract, a, b, c, d) {
            let currentResultArray = await voteContract.getCurrentResult();
            compareResult(currentResultArray, a, b, c, d);
            // check results one by one
            let singleVoteCount = await voteContract.getVoteCountForOption(0);
            singleVoteCount.should.be.eq.BN(a);
            singleVoteCount = await voteContract.getVoteCountForOption(1);
            singleVoteCount.should.be.eq.BN(b);
            singleVoteCount = await voteContract.getVoteCountForOption(2);
            singleVoteCount.should.be.eq.BN(c);
            singleVoteCount = await voteContract.getVoteCountForOption(3);
            singleVoteCount.should.be.eq.BN(d);
            // check total counter
            let totalCount = await voteContract.voteCount();
            totalCount.should.be.eq.BN(a + b + c + d);
        };

        it('should not allow votes for invalid options', async function () {
            let v = await Voting.deployed();
            // we have only option 0-3
            await expectFail(v.castVote('voter1', 4, {from: voter1}), errors.invalidChoice);
            await checkResultsInContract(v, 0, 0, 0, 0);
        });

        it('should not allow votes with too short name', async function () {
            let v = await Voting.deployed();
            // we have only option 0-3
            await expectFail(v.castVote('v', 0, {from: voter1}), errors.nameTooShort);
            await checkResultsInContract(v, 0, 0, 0, 0);
        });

        it('should give a vote for option 0 and emit correct event', async function () {
            let v = await Voting.deployed();
            let {logs} = await v.castVote('voter1', 0, {from: voter1});
            // check if correct log gets emitted:
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 0,
                    allVotes: arrayAsBn(1, 0, 0, 0)
                }
            }]);
        });

        it('should have increased vote counters correctly after vote', async function () {
            let v = await Voting.deployed();
            await checkResultsInContract(v, 1, 0, 0, 0);
        });

        it('should not allow the same voter to vote again', async function () {
            let v = await Voting.deployed();
            await expectFail(v.castVote('voter1', 0, {from: voter1}), errors.alreadyVoted);
            // different vote
            await expectFail(v.castVote('voter1', 1, {from: voter1}), errors.alreadyVoted);
            // results unchanged? (should be guaranteed by EVM, so unnecessary check)
            await checkResultsInContract(v, 1, 0, 0, 0);
        });

        it('should not allow to send ether with vote', async function () {
            let v = await Voting.deployed();
            await expectFailWithoutMsg(v.castVote('voter4', 2, {from: voter4, value: 50}));
            await checkResultsInContract(v, 1, 0, 0, 0);
        });

        it('should update counters correctly after vote for another option', async function () {
            let v = await Voting.deployed();
            let {logs} = await v.castVote('voter2', 3, {from: voter2});
            // check if correct log gets emitted:
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 3,
                    allVotes: arrayAsBn(1, 0, 0, 1)
                }
            }]);
            await checkResultsInContract(v, 1, 0, 0, 1);
        });

        it('should update counters correctly after vote for the same option', async function () {
            let v = await Voting.deployed();
            let {logs} = await v.castVote('voter3', 3, {from: voter3});
            // check if correct log gets emitted:
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 3,
                    allVotes: arrayAsBn(1, 0, 0, 2)
                }
            }]);
            await checkResultsInContract(v, 1, 0, 0, 2);
        });

        it('should return correct voter names', async function () {
            let v = await Voting.deployed();
            let name = await v.getVotersName({from: voter1});
            name.should.be.equal('voter1');
            name = await v.getVotersName({from: voter2});
            name.should.be.equal('voter2');
            name = await v.getVotersName({from: voter3});
            name.should.be.equal('voter3');
            await expectFailWithoutMsg(v.getVotersName({from: voter4}));
        });

        it('should return correct choice per voter', async function () {
            let v = await Voting.deployed();
            let choice = await v.getVotersChoice({from: voter1});
            choice.should.be.eq.BN(0);
            choice = await v.getVotersChoice({from: voter2});
            choice.should.be.eq.BN(3);
            choice = await v.getVotersChoice({from: voter3});
            choice.should.be.eq.BN(3);
            await expectFailWithoutMsg(v.getVotersName({from: voter4}));
        });

        it('should update counters after also after a few votes', async function () {
            let v = await Voting.deployed();

            var {logs} = await v.castVote('voter4', 2, {from: voter4});
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 2,
                    allVotes: arrayAsBn(1, 0, 1, 2)
                }
            }]);
            await checkResultsInContract(v, 1, 0, 1, 2);

            var {logs} = await v.castVote('voter5', 1, {from: voter5});
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 1,
                    allVotes: arrayAsBn(1, 1, 1, 2)
                }
            }]);
            await checkResultsInContract(v, 1, 1, 1, 2);

            var {logs} = await v.castVote('voter6', 3, {from: voter6});
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 3,
                    allVotes: arrayAsBn(1, 1, 1, 3)
                }
            }]);
            await checkResultsInContract(v, 1, 1, 1, 3);
        });
    }
);


