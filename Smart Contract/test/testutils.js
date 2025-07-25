// These 2 consts are used to compare the revert message, with the expected one.
// This can vary depending on the used ethereum client (ganache, geth, ..)
const ERROR_REVERT_PREFIX = "Returned error: VM Exception while processing transaction: revert ";
const ERROR_REVERT_MIDDLE = " -- Reason given: ";
const ERROR_REVERT_SUFFIX = ".";


/**
 * Wraps old functions with callbacks into a Promise
 * to be able to use the nice await syntax.
 *
 * @param inner
 * @return {Promise<any>}
 */
const promisify = (inner) => new Promise((resolve, reject) => inner((err, res) => err ? reject(err) : resolve(res)));

/**
 * Gets balance of an account.
 *
 * @param addr
 * @return {Promise<*>}
 */
const getBalance = async (addr) => web3.utils.toBN(await web3.eth.getBalance(addr));

/**
 * Helper to calculate tx costs of a tx from a receipt. Useful to test
 * if a transfer transferred the correct value (as some eth are lost in fees).
 * @param receipt
 *
 * @return {Promise<*>}
 */
async function calculateFees(receipt) {
    let {gasPrice} = await web3.eth.getTransaction(receipt.transactionHash);
    return web3.utils.toBN(gasPrice * receipt.gasUsed);
}

/**
 * Get time from last block.
 *
 * @return {Promise<number>}
 */
async function getTime() {
    return (await web3.eth.getBlock(await web3.eth.getBlockNumber())).timestamp
}

/**
 * Checks if test is running againdst TestRPC environment (now ganache).
 *
 * @return {Promise<boolean>}
 */
const isTestRPC = async () => (await web3.eth.getNodeInfo()).includes("TestRPC/v2");

/**
 * Helper functicon to assert that a tx fails.
 *
 * @param promise
 * @return {Promise<void>}
 */
const expectFailWithoutMsg = async (promise) => {
    await promise.should.be.rejectedWith('revert');
};

/**
 * Expect promise to be rejected with a given message.
 *
 * @param promise
 * @return {Promise<void>}
 */
const expectFail = async (promise, msg) => {
    assert(msg, "Testsuite error: missing 'msg' argument for 'expectFail'");
    await promise.should.be.rejectedWith(ERROR_REVERT_PREFIX + msg + ERROR_REVERT_MIDDLE + msg + ERROR_REVERT_SUFFIX);
};

/**
 * Check for specific events in the logs of a transaction.
 *
 * @param logs
 * @param template
 */
function checkEvents(logs, template) {
    if (logs.length != template.length) throw new Error('length does not match');
    for (let i = 0; i < logs.length; i++) {
        let log = logs[i];
        let temp = template[i];

        log.event.should.equal(temp.event);

        for (let arg in temp.args) {
            let v = temp.args[arg];
            if (typeof v === 'number') {
                v = web3.utils.toBN(v)
            }

            if (web3.utils.BN.isBN(v)) {
                log.args[arg].should.eq.BN(v)
            } else {
                log.args[arg].should.deep.equal(v)
            }
        }
    }
}

/**
 * Sign message.
 *
 * @param signer
 * @param hash
 * @return {Promise<{r: string, s: string, v: number, sig: (*|ArrayBuffer)}>}
 */
async function sign(signer, hash) {
    const sig = await web3.eth.sign(hash, signer);

    let r = sig.substr(0, 66);
    let s = "0x" + sig.substr(66, 64);
    let v = parseInt(sig.substr(130, 2), 16) + 27;

    return {r, s, v, sig}
}

/**
 * Helper function which travels into the future
 * (only works when testing in ganache/truffle,
 * of course real networks will not allow timetravel.)
 *
 * @param secondsToTravel
 * @return promise
 */
const travelIntoTheFuture = function (secondsToTravel) {
    return promisify(function (cb) {
        web3.currentProvider.send({
            jsonRpc: "2.0",
            method: "evm_increaseTime",
            params: [secondsToTravel],
            id: 0
        }, cb);
    });
};


module.exports = {
    sign,
    checkEvents,
    expectFail,
    expectFailWithoutMsg,
    getTime,
    calculateFees,
    getBalance,
    nulladdress: '0x0000000000000000000000000000000000000000',
    promisify,
    travelIntoTheFuture
};

