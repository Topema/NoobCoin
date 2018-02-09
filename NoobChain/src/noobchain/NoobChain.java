package noobchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class NoobChain {

    private static HashMap<String,TransactionOutput> unspentTransactions = new HashMap<>();
    private static int difficulty;
    private static float minimumTransacion = 0.1f;
    private static BlockList blockList;
    private static Transaction genesisTransaction;

    public static void main(String[] args){

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        BlockList blockList = new BlockList();
        setDifficulty(5);
        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();
        Wallet coinbase = new Wallet();

        System.out.println(blockList.blockListToJson());

        System.out.println("Private and public keys:");
        System.out.println("Wallet A: " + StringUtil.getStringFromKey(walletA.getPrivateKey()));
        System.out.println("\nWallet B: " + StringUtil.getStringFromKey(walletB.getPublicKey()));

        Transaction transaction = new Transaction(walletA.getPublicKey(), walletB.getPublicKey(),5,null);
        transaction.generateSignature(walletA.getPrivateKey());

        System.out.println("Signature verified");
        System.out.println(transaction.verifySignature());

        //create genesis transaction, which sends 100 NoobCoin to walletA:
        genesisTransaction = new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100f, null);
        genesisTransaction.generateSignature(coinbase.getPrivateKey());	 //manually sign the genesis transaction
        genesisTransaction.setTransactionId("0"); //manually set the transaction id
        genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId())); //manually add the Transactions Output
        unspentTransactions.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        try {
            genesis.addTransaction(genesisTransaction);
        } catch (Exception e) {
            e.printStackTrace();
        }

        genesis.mineBlock(difficulty);
        blockList.addBlockToBlockChain(genesis);

        //testing
        Block block1 = new Block(genesis.getHash());
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        try {
            block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40));
        } catch (Exception e) {
            e.printStackTrace();
        }
        block1.mineBlock(difficulty);
        blockList.addBlockToBlockChain(block1);

        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.getHash());
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        try {
            block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        block2.mineBlock(difficulty);
        blockList.addBlockToBlockChain(block2);

        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.getHash());
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        try {
            block3.addTransaction(walletB.sendFunds( walletA.getPublicKey(), 20));
        } catch (Exception e) {
            e.printStackTrace();
        }
        block3.mineBlock(difficulty);
        blockList.addBlockToBlockChain(block3);

        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block4 = new Block(block3.getHash());
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        try {
            block4.addTransaction(walletB.sendFunds( walletA.getPublicKey(), 20));
        } catch (Exception e) {
            e.printStackTrace();
        }
        block3.mineBlock(difficulty);
        blockList.addBlockToBlockChain(block4);

        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block5 = new Block(block4.getHash());
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        try {
            block5.addTransaction(walletA.sendFunds( walletB.getPublicKey(), 50));
        } catch (Exception e) {
            e.printStackTrace();
        }
        block3.mineBlock(difficulty);
        blockList.addBlockToBlockChain(block5);

        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());



        try {
            isChainValid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void isChainValid() throws Exception {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<>();
        TransactionOutput put = tempUTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));

        for(int i=1 ; i <= blockList.getSize(); i++) {

            currentBlock = blockList.getBlock(i);
            previousBlock = blockList.getBlock(i-1);

            hashIrregularitiesDetector(currentBlock, previousBlock, hashTarget);

            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                signatureAndInputsIrregularitiesDetector(t, currentTransaction);

                referencedInputIrregularitiesDetector(tempUTXOs, t, currentTransaction.getInputs());

                for(TransactionOutput output: currentTransaction.getOutputs()) {
                    tempUTXOs.put(output.getId(), output);
                }

                participantsIrregularitiesDetector(currentTransaction);

            }

        }
        System.out.println("Blockchain is valid");
    }

    private static void participantsIrregularitiesDetector(Transaction currentTransaction) throws Exception {
        if( currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
            throw new Exception ("#Transaction(\" + t + \") output reciepient is not who it should be");
        }
        if( currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
            throw new Exception ("#Transaction(\" + t + \") output 'change' is not sender.");
        }
    }

    private static void referencedInputIrregularitiesDetector(HashMap<String, TransactionOutput> tempUTXOs, int t, ArrayList<TransactionInput> currentTransaction) throws Exception {
        TransactionOutput tempOutput;
        for(TransactionInput input: currentTransaction) {
           tempOutput = tempUTXOs.get(input.getTransactionOutputId());

            isReferencedInputTransactionMissed(t, tempOutput);

            isReferencedInputTransactionInvalid(input, tempOutput);

            tempUTXOs.remove(input.getTransactionOutputId());
        }
    }

    private static void isReferencedInputTransactionInvalid(TransactionInput input, TransactionOutput tempOutput) throws Exception {
        if (input.getUTXO().getValue() != tempOutput.getValue()) {
            throw new Exception("#Rederenced input Transaction(\" + t + \") value is Invalid\"");
        }
    }

    private static void isReferencedInputTransactionMissed(int t, TransactionOutput tempOutput) throws Exception {
        if (tempOutput == null) {
            throw new Exception("#Referenced input on Transaction(" + t + ") is Missing");
        }
    }

    private static void signatureAndInputsIrregularitiesDetector(int t, Transaction currentTransaction) throws Exception {
        if(!currentTransaction.verifySignature()) {
            throw new Exception("#Signature on Transaction(" + t + ") is Invalid");
        }
        if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
            throw new Exception("#Inputs are not equal to outputs on Transaction(" + t + ")");
        }
    }

    private static void hashIrregularitiesDetector(Block currentBlock, Block previousBlock, String hashTarget) throws Exception {
        //compare registered hash and calculated hash:
        if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
            throw new Exception("#Current Hashes not equal");
        }
        //compare previous hash and registered previous hash
        if(!previousBlock.getHash().equals(currentBlock.getPreviousHash()) ) {
            throw new Exception("#Previous Hashes not equal");
        }
        //check if hash is solved
        if(!currentBlock.getHash().substring( 0, difficulty).equals(hashTarget)) {
            throw new Exception("#This Block has not been mined");
        }
    }

    public static HashMap<String, TransactionOutput> getUnspentTransactions() {
        return unspentTransactions;
    }

    private static void setDifficulty(int difficulty) {
        NoobChain.difficulty = difficulty;
    }

    public static float getMinimumTransacion() {
        return minimumTransacion;
    }
}