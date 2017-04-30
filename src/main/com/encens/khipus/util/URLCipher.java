package com.encens.khipus.util;

import com.encens.khipus.exception.UrlCipherException;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import static org.jboss.seam.ScopeType.APPLICATION;

/**
 * Encrypt and decrypt using the TripleDES encryption algorithm
 * and a key specification for the secret key.
 *
 * @author
 * @version 1.0
 */
@Name("cipher")
@Scope(APPLICATION)
@Startup
public class URLCipher {
    private static final String ALGORITHM = "DESede";
    private static final String spec = "rJruhsTs45retElrFERmotor";//TODO: put this key in configuration file
    //TODO: http://localhost:8080/khipus/admin/language.jsf?name=wl6wSe6NjRY*&id=x5JyJWpACE4*&cid=8 does not work (if it is not logged in)
    private SecretKey secretKey;
    private UrlBase64Encoder urlBase64Encoder;
    Cipher cipher;

    protected static final LogProvider log = Logging.getLogProvider(URLCipher.class);


    @Create
    public void init() {
        try {
            log.debug("initializing cipher");
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            KeySpec keySpec = new DESedeKeySpec(spec.getBytes());
            secretKey = keyFactory.generateSecret(keySpec);
            cipher = Cipher.getInstance(ALGORITHM);
            urlBase64Encoder = new UrlBase64Encoder();
        } catch (NoSuchAlgorithmException e) {
            log.error("The algorithm = " + ALGORITHM + " is not supported", e);
            throw new UrlCipherException("The algorithm = " + ALGORITHM + " is not supported", e);
        } catch (NoSuchPaddingException e) {
            log.error("Error creating the cipher", e);
            throw new UrlCipherException("Error creating the cipher", e);
        } catch (InvalidKeyException e) {
            log.error("Invalid key", e);
            throw new UrlCipherException("the key is invalid", e);
        } catch (InvalidKeySpecException e) {
            log.error("Invalid key specification", e);
            throw new UrlCipherException("Invalid key specification", e);
        }
    }

    /**
     * Encrypt a string and then encode in base64 for url
     *
     * @param str string to be encrypted
     * @return encrypted string
     */
    public String encrypt(String str) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptText = cipher.doFinal(str.getBytes());
            return urlBase64Encoder.encode(encryptText);
        } catch (InvalidKeyException e) {
            throw new UrlCipherException("Error on encryption", e);
        } catch (BadPaddingException e) {
            throw new UrlCipherException("Error on encryption", e);
        } catch (IllegalBlockSizeException e) {
            throw new UrlCipherException("Error on encryption", e);
        } catch (Exception e) {
            throw new UrlCipherException("Error on encryption", e);
        }
    }

    /**
     * Decrypt an encrypted string
     *
     * @param str string to be decrypted
     * @return the original decrypted string.
     */
    public String decrypt(String str) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedText = urlBase64Encoder.decode(str);
            return new String(cipher.doFinal(decodedText));
        } catch (InvalidKeyException e) {
            throw new UrlCipherException("Error on decryption", e);
        } catch (BadPaddingException e) {
            throw new UrlCipherException("Error on decryption", e);
        } catch (IllegalBlockSizeException e) {
            throw new UrlCipherException("Error on decryption", e);
        } catch (Exception e) {
            throw new UrlCipherException("Error on decryption", e);
        }
    }


}
