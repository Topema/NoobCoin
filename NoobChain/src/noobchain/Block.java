package noobchain;

import java.util.ArrayList;
import java.util.Date;

import static noobchain.StringUtil.applySha256AlgorithmToString;

public class Block {
    private String hash;
    private String previousHash;
    private String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private long timeStamp;
    public int nonce;

    public Block(String previousHash){
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash= calculateHash();
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String calculateHash() {
        String sha256Input =
                Long.toString(timeStamp) +previousHash +
                Integer.toString(nonce) +
                merkleRoot;
        String calculatedHash = applySha256AlgorithmToString(sha256Input);
        return calculatedHash;
    }

    public void mineBlock(int difficulty){
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0,difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    public boolean addTransaction(Transaction transaction) throws Exception {
        if(transaction == null) return false;
        if((previousHash != "0")){
            transaction.processTransaction();
        }
        transactions.add(transaction);
        System.out.println("Transaction Succesfully added to Block");
        return true;
    }
}
