# Smart Contract for simple Voting Example

This subdirectory contains an Ethereum Smart Contract example,
which implements a very simple and basic voting contract.

This contract has been deployed to the public ethereum blockchain
at address: `TODO`


### Requirements

This was developed using Node v10.12.0 and Truffle v5.0.0-next.11.

(Since v5 truffle supports async/await syntax which makes life easier
in writing tests and also uses web3 1.0. Be aware thet web3 1.0 uses
the bn.js library instead of BigNumber.js)


```

# check for recent node version (8.x or 10.x)
node -v

# Install Truffle v5.0.0 or higher (currently in beta)
npm install -g truffle@beta
# or
npm install -g truffle

# check version
truffle --version

# save dependencies in package.json
npm install --save chai bn-chai chai-as-promised


```


### Test


```
# start truffle develop console (will start its own Ethereum implementation)
truffle develop

# Interact with contract interactively
...


# Deploying to ropsten (with Migration.sol)
truffle console --network ropsten

truffle(ropsten)> migrate

# or redeploy:
migrate --reset

```


### Ropsten Testnet

Deployed on Ropsten at: https://ropsten.etherscan.io/address/0xe96398ece7be0b03b53f1ca01a23698db338cc5d
