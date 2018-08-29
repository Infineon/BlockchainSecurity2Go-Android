var Voting = artifacts.require("./Voting.sol");

contract('Voting', function (accounts) {
    it("vote", function () {
        var meta;

        var account_one = accounts[0];

        return Voting.deployed().then(function (instance) {
            meta = instance;
            return meta.giveVote.call("pts", 1, {from: account_one});
        }).then(function (voted) {
            assert.equal(voted.valueOf(), true, "Could not add new voter");
            return meta.getVote.call({from: account_one});
        }).then(function (vote) {
            assert.equal(vote.valueOf(), 1, "Incorrect vote returned");
        })
    });

    // it("voteeee", function() {
    //     var meta;
    //
    //     return Voting.deployed().then(function(instance) {
    //         meta = instance;
    //         return meta.giveVote.call("pts",1);
    //     }).then(function(voted) {
    //         assert.equal(voted.valueOf(), true, "Could not add new voter");
    //         return meta.getVote.call();
    //     }).catch(function(error) {
    //       console.log("gfggggg"+error.reason);
    //         if(error.toString().indexOf("VM Exception while processing transaction: revert") != -1) {
    //             console.log("We were expecting a Solidity throw (aka an invalid JUMP), we got one. Test succeeded.");
    //         } else {
    //             // if the error is something else (e.g., the assert from previous promise), then we fail the test
    //             assert(false, error.toString());
    //         }
    //     });
    // });

    // it("check vote", function() {
    //   return Voting.deployed().then(function(instance) {
    //     return instance.getVote.call();
    //   }).then(function(vote) {
    //     assert.equal(vote.valueOf(), 1, "0 wasn't in the first account");
    //   });
    // });
});
