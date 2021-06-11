package com.example.minachatapp;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;

import javax.crypto.Cipher;

public class Message {
    private String username;
    private String content;
    private PrivateKey myPriKey;
    private PublicKey guestPubKey;
    private int tagNumber = 0;
    private String resContent;

    public Message(String content, String username, PrivateKey myPriKey, PublicKey guestPubKey, int tagNumber) {
        this.content = content;
        this.username = username;
        this.myPriKey = myPriKey;
        this.guestPubKey = guestPubKey;
        this.tagNumber = tagNumber;
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public String getUsername() {
        return this.username;
    }

    public String getContent() {
        return this.resContent;
    }

    public String getOriginalContent() { return this.content; }

    public int getTagNumber() {
        return this.tagNumber;
    }

    public String createSecuredMsg() throws Exception {
        String originalMsg = username + "---" + tagNumber + "---" + content + "---" + "END";
        String encryptedMsg = encryptContent(originalMsg, guestPubKey);
        String signature = createSignature(originalMsg, myPriKey);
        resContent = encryptedMsg + "---" + signature;
        return resContent;
    }

    public void readSecuredMsg() throws Exception {
        String[] msg = content.split("---", 2);
        String originalMsg = decryptContent(msg[0], myPriKey);
        String[] msgContent = originalMsg.split("---", 4);
        if(msgContent[3].equals("END")) {
            boolean check = verifySignature(originalMsg, guestPubKey, msg[1]);
            System.out.println("\n" + originalMsg);
            System.out.println(check + "--" + msgContent[0].equals(username));
            if(check) {
                if(msgContent[0].equals(username)) {
                    tagNumber = Integer.valueOf(msgContent[1]);
                    resContent = msgContent[2];
                    return;
//                    return tagNumber + "---" + resContent; //tagNumber + Msg
                }
            }
        }
        resContent = null;
        return;
//        return null;
    }

    private String encryptContent(String content, PublicKey pubKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        byte[] cipherTxt = cipher.doFinal(content.getBytes());
        String encryptedContent = Hex.toHexString(cipherTxt);
        return encryptedContent;
    }

    private String decryptContent(String content, PrivateKey priKey) throws Exception {
        byte[] data = Hex.decode(content);

        Cipher cipher = Cipher.getInstance("ECIES");
        cipher.init(Cipher.DECRYPT_MODE, priKey);

        byte[] txt = cipher.doFinal(data);
        String originalContent = new String(txt);
        return originalContent;
    }

    private String createSignature(String content, PrivateKey priKey) throws Exception {
        //Create hash of content
        MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");
        msgDigest.update(content.getBytes());
        byte[] byteHash = msgDigest.digest();

        //Create signature by encrypting hash value with our private key
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(priKey);
        signature.update(byteHash);
        return Hex.toHexString(signature.sign());
    }

    private boolean verifySignature(String content, PublicKey pubKey, String signature) throws Exception {
        // Create hash based on msg we decrypt using our private key before
        MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");
        msgDigest.update(content.getBytes());
        byte[] ourByteHash = msgDigest.digest();

        // Verify signature using guest public key
        Signature signatureObj = Signature.getInstance("SHA256withECDSA");
        signatureObj.initVerify(pubKey);
        signatureObj.update(ourByteHash);
        return signatureObj.verify(Hex.decode(signature));
    }
}
