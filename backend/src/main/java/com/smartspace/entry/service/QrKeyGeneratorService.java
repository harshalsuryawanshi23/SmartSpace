package com.smartspace.entry.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.*;

@Slf4j
@Service
public class QrKeyGeneratorService {

    @Getter
    private PrivateKey privateKey;

    @Getter
    private PublicKey publicKey;

    @Getter
    private String keyId;

    @PostConstruct
    public void init() {
        try {
            // Generating an in-memory key pair for now. In production, we would load from environment.
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
            KeyPair kp = kpg.generateKeyPair();
            this.privateKey = kp.getPrivate();
            this.publicKey = kp.getPublic();
            this.keyId = "k1";
            log.info("Ed25519 KeyPair generated with keyId: {}", keyId);
        } catch (NoSuchAlgorithmException e) {
            log.error("Ed25519 algorithm not found. Make sure you are on Java 15+", e);
            throw new RuntimeException(e);
        }
    }
}
