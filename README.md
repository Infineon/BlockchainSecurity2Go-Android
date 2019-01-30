Coinfinity Blockchain Security 2Go example app
===


This project contains an Android app showcasing interaction with the Ethereum blockchain using 
the Infineon Blockchain Security 2Go starter kit NFC card.

Currently three main features are available to demonstrate signing of Ethereum transactions 
using the Infineon Blockchain Security 2Go starter kit NFC card:  
- sending ETH
- sending ERC-20 tokens
- Interacting with an example smart contract (voting demo) deployed an the Ethereum blockchain.  
 
How it works
---

The app is communicating with the card via NFC to generate new private/public keys, 
read the public keys and sign transactions. To interact with the Ethereum blockchain, 
the [Web3j library](https://github.com/web3j/web3j) is used to build transactions. 
Brodacasting of transactions to the Ethereum network is done via the 
[infura.io API](https://infura.io/docs).
The conversion of ETH balance to EUR is done via Coinfinity rate API and displayed 
inside the app.

To be able to use the QR code scanning and displaying functionality the app expects the 
`com.google.zxing.client.android` QR code scanner to be installed ([Play Store](https://play.google.com/store/apps/details?id=com.google.zxing.client.android))

Getting started
---

- Import the project into Intellij or Android Studio, use project type "Gradle"
- Try to build it
- Make sure to install Zxing barcode scanner app on device to be able to scan QR codes
- Add an android run/debug configuration in Intellij/Android Studio
- Run/debug app via Intellij/Android Studio
  

Android Device requirements
---

Do not use an Android emulator for testing purposes because NFC is required to interact with Infineon Card.
- minSdkVersion 26  
- targetSdkVersion 28  
- NFC enabled device
- internet connection
- camera for QR code scanning

Smart Contract
---

The example smart contract is located in a separate folder outside the app project called 
"Smart Contract". It contains Solidity code for the contract and a truffle suite for testing.  
[Voting.sol](Smart%20Contract/contracts/Voting.sol) contains the main code of the contract 
and imports other common ".sol" files. See the [Solidity language documentation](https://solidity.readthedocs.io/en/latest/) 
on further information on how to write Smart Contracts in Solidity.

[voting.js](Smart%20Contract/test/voting.js) is the main truffle test file written in JavaScript to quickly test the contract 
without deploying it on mainnet or testnet. See the [truffle documentation](https://truffleframework.com/docs/)
on further information on how to write tests for Solidity contracts.