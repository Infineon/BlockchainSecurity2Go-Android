var Voting = artifacts.require("./Voting.sol");

contract('Voting', function (accounts) {
    let catchRevert = require("./exceptions.js").catchRevert;
    var meta;

    it("should give a vote and check if voted", function () {
        return Voting.deployed().then(function (instance) {
            meta = instance;
            return meta.giveVote("voter1", 3, {from: accounts[0]});
        }).then(function () {
            return meta.getVotersAnswer.call({from: accounts[0]});
        }).then(function (vote) {
            assert.equal(vote.valueOf(), 3, "Incorrect voters answer returned");
        }).then(function () {
            return meta.getVotersName.call({from: accounts[0]});
        }).then(function (vote) {
            assert.equal(vote.valueOf(), "voter1", "Incorrect voters name returned");
        })
    });
    it("should not allow second vote", function () {
        return Voting.deployed().then(function (instance) {
            meta = instance;
            return meta.giveVote("voter2", 1, {from: accounts[1]});
        }).then(async function () {
            await catchRevert(meta.giveVote("voter2", 1, {from: accounts[1]}));
        });
    });
    it("should not allow voter name with too less chars", function () {
        return Voting.deployed().then(async function (instance) {
            meta = instance;
            await catchRevert(meta.giveVote("vo", 1, {from: accounts[2]}));
        });
    });
    it("should not allow give vote with not allowed answer", function () {
        return Voting.deployed().then(async function (instance) {
            meta = instance;
            await catchRevert(meta.giveVote("vo", 9999, {from: accounts[3]}));
        });
    });
    it("should give all vote counts back", function () {
        return Voting.deployed().then(function (instance) {
            meta = instance;
            return meta.getAnswerCounts.call({from: accounts[0]});
        }).then(function (answerCount) {
            assert.equal(answerCount[0].toNumber(), 0, "Incorrect voter count returned");
            assert.equal(answerCount[1].toNumber(), 1, "Incorrect voter count returned");
            assert.equal(answerCount[2].toNumber(), 0, "Incorrect voter count returned");
            assert.equal(answerCount[3].toNumber(), 1, "Incorrect voter count returned");
        })
    });
});
