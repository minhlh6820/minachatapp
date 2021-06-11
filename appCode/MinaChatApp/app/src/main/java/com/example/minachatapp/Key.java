package com.example.minachatapp;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Key {
    public KeyPair generateKeyPair() throws Exception {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
        KeyPair pair = keyGen.generateKeyPair();
        return pair;
    }

    public String getPubKeyStr(KeyPair keyPair) {
        PublicKey pubKey = keyPair.getPublic();
        byte[] byte_pubKey = pubKey.getEncoded();
        String str_pubKey = Hex.toHexString(byte_pubKey);
        return str_pubKey;
    }

    public String getPriKeyStr(KeyPair keyPair) {
        PrivateKey priKey = keyPair.getPrivate();
        byte[] byte_priKey = priKey.getEncoded();
        String str_priKey = Hex.toHexString(byte_priKey);
        return str_priKey;
    }

    public PublicKey getPubKeyFromStr(String pubKeyStr) throws Exception {
        byte[] bytePubKey = Hex.decode(pubKeyStr);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(bytePubKey));
        return pubKey;
    }

    public PrivateKey getPriKeyFromStr(String priKeyStr) throws Exception {
        byte[] bytePriKey = Hex.decode(priKeyStr);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PrivateKey priKey = kf.generatePrivate(new PKCS8EncodedKeySpec(bytePriKey));
        return priKey;
    }
}
