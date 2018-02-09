package noobchain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Wallet {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //only UTXOs owned by this wallet.

    public Wallet(){
        generateKeyPair();
    }

    private void generateKeyPair() {
        try{
            KeyPairGenerator keyGeneration = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            //Initialize the key generator and generate keyPair
            keyGeneration.initialize(ecSpec, random);
            KeyPair keyPair = keyGeneration.generateKeyPair();

            //Set the public and private keys from the keyPair
            setPrivateKey(keyPair.getPrivate());
            setPublicKey(keyPair.getPublic());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    private void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item:
                NoobChain.getUnspentTransactions().entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)){
                UTXOs.put(UTXO.getId(),UTXO);
                total += UTXO.getValue();
            }
        }
        return total;
    }

    public Transaction sendFunds(PublicKey recipient, float value) throws Exception {
        if(getBalance() < value) {
            throw new Exception("#Not enough funds to send transaction. Transaction Discarded");
        }
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransactionInput(UTXO.getId()));
            if(total > value){
                break;
            }
        }

        Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs){
            UTXOs.remove(input.getTransactionOutputId());
        }
        return newTransaction;
    }
}
