Coinfinity Android App
===


This project contains functionality interacting with the Ethereum Blockchain via Infineon Card.  
Currently three main features are available to demonstrate signing of Ethereum transactions via Infineon Card.  
- sending ETH via card
- sending ER20 tokens via card
- voting via Smart Contract deployed at Ethereum Blockchain via card
 
How it work
---

The app is communicating with the card via NFC to generate new private/public keys, read a public key and sign transactions.  
To interact with the Ethereum blockchain Web3j is used for sending transaction, reading balance, use smart contract and so on.  
Euro balance is read via Coinfinity API and displayed inside the app.  
QR codes can be scanned for usability reasons to make sending ETH/ERC20 tokens or voting on contract faster. com.google.zxing.client.android app is used for that.  
Zxing barcode/QR Code Generator is used to create QR Code of Ethereum address related to public/private key of Infineon card.

Getting started
---

- Import the project into Intellij, use "Gradle"
- Try to build it
- Make sure installing Zxing barcode scanner app on device to be able to scan qr codes
- Add an android run/debug configuration in Intellij
- Run/debug app via Intellij
  
Android Device requirements
---

Do not use an Android Emulator for testing purposes because NFC is rquired to interact with Infineon Card.
- minSdkVersion 19  
- targetSdkVersion 27  

Smart Contract
---

The smart contract is located in a separate folder outside the app project called "Smart Contract". It contains Solidity code for the contract and a truffle suit for testing.  
Voting.sol contains the main code of the contract and imports other common ".sol" files.  
voting.js is the main truffle test file written in JavaScript to quickly test the contract without deploying it on mainnet or testnet.