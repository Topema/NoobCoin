package noobchain;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class StringUtil {

    static String applySha256AlgorithmToString(String input){
        try{
            byte[] hash = HashGeneratorForInput(input);
            return HashToString(hash);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static byte[] HashGeneratorForInput(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest CryptographicHashFunction = MessageDigest.getInstance("SHA-256");
        return CryptographicHashFunction.digest(input.getBytes("UTF-8"));
    }

    private static String HashToString(byte[] hash) {
        StringBuffer hexadecimalString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String HexadecimalHashCharacter = Integer.toHexString(0xff & hash[i]);
            if(HexadecimalHashCharacter.length()==1) hexadecimalString.append('0');
            hexadecimalString.append(HexadecimalHashCharacter);
        }
        return hexadecimalString.toString();
    }

    public static byte[] applyEllipticCurveSignatureAlgorithm(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output = new byte[0];
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] ByteString = input.getBytes();
            dsa.update(ByteString);
            byte[] realSignature = dsa.sign();
            output = realSignature;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static boolean verifyEllipticCurveSignatureAlgorithm(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMerkleRoot(ArrayList<Transaction> transactions){
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for (Transaction transaction : transactions){
            previousTreeLayer.add(transaction.getTransactionId());
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while(count >1){
            treeLayer = new ArrayList<String>();
            for (int i = 1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(applySha256AlgorithmToString(previousTreeLayer.get(i-1))+ previousTreeLayer.get(i));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() ==1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}
