var Voting = artifacts.require("./Voting.sol");

contract('Voting', function (accounts) {
    let catchRevert = require("./exceptions.js").catchRevert;
    var meta;

    it("should give a vote and check if voted", function () {
        return Voting.deployed().then(function (instance) {
            meta = instance;
            return meta.giveVote("voter1", 3, {from: accounts[0]});
        }).then(function () {
            return meta.getVote.call({from: accounts[0]});
        }).then(function (vote) {
            assert.equal(vote.valueOf(), 3, "Incorrect vote returned");
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
});
