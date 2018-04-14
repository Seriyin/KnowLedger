package pt.um.lei.masb.blockchain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ident {
    private static Logger LOGGER = Logger.getLogger("Ident");

    //Ensure Bouncy Castle Crypto provider is present
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    private PrivateKey privateKey;
    private PublicKey publicKey;


    public Ident() {
        generateKeyPair();
    }

    private void generateKeyPair() {
        try {
            var keygen = KeyPairGenerator.getInstance("ECDSA", "BC");
            var random = SecureRandom.getInstance("SHA1PRNG");
            var ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keygen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
            KeyPair keyPair = keygen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException("Keygen problem", e);
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
