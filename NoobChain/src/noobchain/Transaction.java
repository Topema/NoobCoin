package noobchain;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {

    private String transactionId;
    private PublicKey sender;
    private PublicKey recipient;
    private float value;
    private byte[] signature;

    private ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    private ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequenceOfTransactions = 0;

    public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
        this.setSender(from);
        this.setRecipient(to);
        this.setValue(value);
        this.setInputs(inputs);
    }

    private String calulateHash() {
        sequenceOfTransactions++; //increase the sequence to avoid 2 identical transactions having the same hash
        String sha256Input = getSenderAndRecipientStringsFromKey() +
                Float.toString(getValue()) + sequenceOfTransactions;
        return StringUtil.applySha256AlgorithmToString(sha256Input);
    }

    public void generateSignature(PrivateKey privateKey){
        String data = getSenderAndRecipientStringsFromKey();
        setSignature(StringUtil.applyEllipticCurveSignatureAlgorithm(privateKey,data));
    }

    public boolean verifySignature(){
        String data = getSenderAndRecipientStringsFromKey();
        return StringUtil.verifyEllipticCurveSignatureAlgorithm(getSender(), data, getSignature());
    }

    private String getSenderAndRecipientStringsFromKey() {
        return StringUtil.getStringFromKey(getSender()) + StringUtil.getStringFromKey(getRecipient());
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public boolean processTransaction() throws Exception {
        if(!verifySignature()){
            throw new Exception("#Transaction Signature failed to verify");
        }

        for (TransactionInput i: getInputs()) {
            i.setUTXO(NoobChain.getUnspentTransactions().get(i.getTransactionOutputId()));
        }

        if(getInputsValue() < NoobChain.getMinimumTransacion()){
            throw new Exception("#Transaction Inputs to small" + getInputsValue());
        }

        float leftOver = getInputsValue() - getValue();
        setTransactionId(calulateHash());
        getOutputs().add(new TransactionOutput(this.getRecipient(), getValue(), getTransactionId()));
        getOutputs().add(new TransactionOutput(this.getSender(), leftOver, getTransactionId()));

        for (TransactionOutput o: getOutputs()) {
            NoobChain.getUnspentTransactions().put(o.getId(), o);
        }

        for(TransactionInput i: getInputs()){
            if(i.getUTXO() == null) {
                continue;
            }
            NoobChain.getUnspentTransactions().remove(i.getUTXO().getId());
        }

        return true;
    }

    public float getInputsValue(){
        float total = 0;
        for (TransactionInput i: getInputs()) {
            if(i.getUTXO() == null){
                continue;
            }
            total += i.getUTXO().getValue();
        }
        return total;
    }

    public float getOutputsValue(){
        float total = 0;
        for (TransactionOutput o : getOutputs()){
            total += o.getValue();
        }
        return total;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public ArrayList<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(ArrayList<TransactionOutput> outputs) {
        this.outputs = outputs;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public void setRecipient(PublicKey recipient) {
        this.recipient = recipient;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(ArrayList<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public PublicKey getSender() {
        return sender;
    }

    public void setSender(PublicKey sender) {
        this.sender = sender;
    }
}
