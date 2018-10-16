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
    ownerZeroNotAllowed: 'New owner cannot be 0x0.',
    onlyWhitelistedCanVote: 'Only whitelisted sender addresses can cast votes.'
};


contract('Vote: init checks', function (accounts) {

        // define some accounts and give them readable names
        const [owner, voter1, voter2, voter3, otherAcc1, otherAcc2, otherAcc3] = accounts;

        it('should have 4 possible choices', async function () {
            let v = await Voting.deployed();

            (await v.numberOfPossibleChoices()).should.eq.BN(4);
        });

        it('should have 0 votes after deployment', async function () {
            let v = await Voting.deployed();
            (await v.voteCountTotal()).should.be.eq.BN(0);
        });

        it('should return 4 entries in currentResult', async function () {
            let v = await Voting.deployed();
            (await v.currentResult()).length.should.be.eq.BN(4);
        });

        it('should have only nulladdress in whitelist at beginning', async function () {
            let v = await Voting.deployed();
            let whitelist = await v.whitelistedSenderAddresses();
            whitelist.length.should.be.eq.BN(4);
            whitelist[0].should.be.equal(nulladdress);
            whitelist[1].should.be.equal(nulladdress);
            whitelist[2].should.be.equal(nulladdress);
            whitelist[3].should.be.equal(nulladdress);
        });

        it('should have 0 in all 4 entries in currentResult', async function () {
            let v = await Voting.deployed();
            let currentResultArray = await v.currentResult();
            currentResultArray[0].should.be.eq.BN(0);
            currentResultArray[1].should.be.eq.BN(0);
            currentResultArray[2].should.be.eq.BN(0);
            currentResultArray[3].should.be.eq.BN(0);
        });

        it('should have 0 in all 4 choice counters (queried separately)', async function () {
            let v = await Voting.deployed();
            let singleVoteCount = await v.votesPerChoice(0);
            singleVoteCount.should.be.eq.BN(0);
            singleVoteCount = await v.votesPerChoice(1);
            singleVoteCount.should.be.eq.BN(0);
            singleVoteCount = await v.votesPerChoice(2);
            singleVoteCount.should.be.eq.BN(0);
            singleVoteCount = await v.votesPerChoice(3);
            singleVoteCount.should.be.eq.BN(0);
        });

        it('should return error if asking for vote counter of non-existing choice', async function () {
            let v = await Voting.deployed();
            await expectFailWithoutMsg(v.votesPerChoice(4));
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
        const [owner, voter1, voter2, voter3, voter4, otherAcc1, otherAcc2, otherAcc3] = accounts;

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

        it('should only allow to be reset by owner', async function () {
            let v = await Voting.deployed();
            // should fail for other
            await expectFail(v.resetDemo({from: otherAcc1}), errors.onlyOwner);
            // should work for owner
            var {logs} = await v.resetDemo({from: owner});
            checkEvents(logs, [{
                event: 'DemoResetted',
                args: {}
            }]);
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

        it('should not allow to set whitelist by non-owner', async function () {
            let v = await Voting.deployed();
            let whitelist = [voter1, voter2, voter3, voter4];
            await expectFail(v.setWhiteList(whitelist, {from: otherAcc1}), errors.onlyOwner);

        });

        it('should allow to set whitelist by owner', async function () {
            let v = await Voting.deployed();
            let whitelist = [voter1, voter2, voter3, voter4];
            var {logs} = await v.setWhiteList(whitelist, {from: owner});
            checkEvents(logs, [{
                event: 'WhitelistUpdated',
                args: {
                    whitelistedSenderAdresses: whitelist
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

        it('should allow to be destructed by owner', async function () {
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
        const [owner, voter1, voter2, voter3, voter4, noVoter1, noVoter2] = accounts;

        /**
         * Small helper functiom that returns the 4 digits as a BN.js array (like web3 returns it)
         */
        const arrayAsBn = function (a, b, c, d) {
            return [web3.utils.toBN(a), web3.utils.toBN(b), web3.utils.toBN(c), web3.utils.toBN(d)];
        };

        /**
         * helper to compare results
         */
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
            let currentResultArray = await voteContract.currentResult();
            compareResult(currentResultArray, a, b, c, d);
            // check results one by one
            let singleVoteCount = await voteContract.votesPerChoice(0);
            singleVoteCount.should.be.eq.BN(a);
            singleVoteCount = await voteContract.votesPerChoice(1);
            singleVoteCount.should.be.eq.BN(b);
            singleVoteCount = await voteContract.votesPerChoice(2);
            singleVoteCount.should.be.eq.BN(c);
            singleVoteCount = await voteContract.votesPerChoice(3);
            singleVoteCount.should.be.eq.BN(d);
            // check total counter
            let totalCount = await voteContract.voteCountTotal();
            totalCount.should.be.eq.BN(a + b + c + d);
        };

        // Tests start here:

        it('should set whitelist correctly', async function () {
            let v = await Voting.deployed();
            // set whitelist and check event:
            let whitelist = [voter1, voter2, voter3, voter4];
            var {logs} = await v.setWhiteList(whitelist, {from: owner});
            checkEvents(logs, [{
                event: 'WhitelistUpdated',
                args: {
                    whitelistedSenderAdresses: whitelist
                }
            }]);
            // read whitelist and compare:
            let readWhitelist = await v.whitelistedSenderAddresses({from: noVoter2});
            readWhitelist.length.should.be.eq.BN(4);
            readWhitelist[0].should.be.equal(voter1);
            readWhitelist[1].should.be.equal(voter2);
            readWhitelist[2].should.be.equal(voter3);
            readWhitelist[3].should.be.equal(voter4);
        });


        it('should not allow votes from other addresses', async function () {
            let v = await Voting.deployed();
            // we have only option 0-3
            await expectFail(v.castVote({from: noVoter1}), errors.onlyWhitelistedCanVote);
            await checkResultsInContract(v, 0, 0, 0, 0);
        });

        it('should give a vote for option 0 and emit correct event', async function () {
            let v = await Voting.deployed();
            let {logs} = await v.castVote({from: voter1});
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

        it('should not allow to send ether with vote', async function () {
            let v = await Voting.deployed();
            await expectFailWithoutMsg(v.castVote({from: voter1, value: 50}));
            await checkResultsInContract(v, 1, 0, 0, 0);
        });

        it('should update counters correctly after vote for another option', async function () {
            let v = await Voting.deployed();
            let {logs} = await v.castVote({from: voter4});
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
            let {logs} = await v.castVote({from: voter4});
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

        it('should update counters after also after a few votes', async function () {
            let v = await Voting.deployed();

            var {logs} = await v.castVote({from: voter3});
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 2,
                    allVotes: arrayAsBn(1, 0, 1, 2)
                }
            }]);
            await checkResultsInContract(v, 1, 0, 1, 2);

            var {logs} = await v.castVote({from: voter2});
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 1,
                    allVotes: arrayAsBn(1, 1, 1, 2)
                }
            }]);
            await checkResultsInContract(v, 1, 1, 1, 2);

            var {logs} = await v.castVote({from: voter4});
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 3,
                    allVotes: arrayAsBn(1, 1, 1, 3)
                }
            }]);
            await checkResultsInContract(v, 1, 1, 1, 3);
        });

        it('should reset all votes after calling resetDemo function', async function () {
            let v = await Voting.deployed();

            var {logs} = await v.resetDemo({from: owner});
            checkEvents(logs, [{
                event: 'DemoResetted',
                args: {}
            }]);
            // now all results should be 0 again
            await checkResultsInContract(v, 0, 0, 0, 0);
            // as well as total count of votes
            let totalCount = await v.voteCountTotal();
            totalCount.should.be.eq.BN(0);
        });

        it('should update counters again when voting after reset', async function () {
            let v = await Voting.deployed();

            var {logs} = await v.castVote({from: voter3});
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 2,
                    allVotes: arrayAsBn(0, 0, 1, 0)
                }
            }]);
            await checkResultsInContract(v, 0, 0, 1, 0);

            var {logs} = await v.castVote({from: voter2});
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 1,
                    allVotes: arrayAsBn(0, 1, 1, 0)
                }
            }]);
            await checkResultsInContract(v, 0, 1, 1, 0);

            var {logs} = await v.castVote({from: voter4});
            checkEvents(logs, [{
                event: 'NewVote',
                args: {
                    addedVote: 3,
                    allVotes: arrayAsBn(0, 1, 1, 1)
                }
            }]);
            await checkResultsInContract(v, 0, 1, 1, 1);
        });
    }
);


contract('Vote: test rescuing ERC-20', function (accounts) {

        const DummyERC20 = artifacts.require('./testutiles/DummyERC20.sol');

        // define some accounts and give them readable names
        const [owner, acc1, acc2] = accounts;

        const totalNumOfTokens = 500000000;

        it('should be possible to rescue ERC-20 tokens', async function () {
            let v = await Voting.deployed();
            let erc20 = await DummyERC20.deployed();
            let voteContractAddr = await v.address;
            let erc20ContractAddr = await erc20.address;

            // initially ERC-20 balance of contract should be 0
            (await erc20.balanceOf(voteContractAddr)).should.be.eq.BN(0);
            // and also of the other addr
            (await erc20.balanceOf(acc1)).should.be.eq.BN(0);
            // and also of the owner
            (await erc20.balanceOf(owner)).should.be.eq.BN(0);

            // the owner can mint test-tokens out of thin air, so lets do this:
            await erc20.mint(acc1, totalNumOfTokens);

            // acc1 should have balance now:
            (await erc20.balanceOf(acc1)).should.be.eq.BN(totalNumOfTokens);

            // now send tokens into the Vote contract address
            await erc20.transfer(voteContractAddr, totalNumOfTokens, {from: acc1});

            // tokens should now have moved to the vote contract
            (await erc20.balanceOf(acc1)).should.be.eq.BN(0);
            (await erc20.balanceOf(voteContractAddr)).should.be.eq.BN(totalNumOfTokens);

            // and now the Vote contract owner should be able to rescue
            // the tokens out of the contract to himself
            await v.recoverTokens(erc20ContractAddr);

            // tokens in contract should now be 0 again and
            // balance of owner should have increased
            (await erc20.balanceOf(voteContractAddr)).should.be.eq.BN(0);
            (await erc20.balanceOf(owner)).should.be.eq.BN(totalNumOfTokens);
        });
    }
);

