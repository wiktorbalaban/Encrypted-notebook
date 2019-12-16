package com.example.encryptednotebook.CipherFacade;

import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;

import com.example.encryptednotebook.Cipher.AndroidKeyStoreAsyncCipher;
import com.example.encryptednotebook.Cipher.AsyncCipher;
import com.example.encryptednotebook.Cipher.CipherException;
import com.example.encryptednotebook.InitialVector.SharedPreferencesInitialVector;
import com.example.encryptednotebook.SecretKey.AndroidKeyStoreSecretKeyProvider;

public class AndroidKeyStoreEncryptionCipherFacade implements RandomizedEncryptionCipherFacade {

    private AndroidKeyStoreAsyncCipher cipher;
    private SharedPreferencesInitialVector iv;
    DecryptFinishEventListener decryptFinishEventListener;
    EncryptFinishEventListener encryptFinishEventListener;

    public AndroidKeyStoreEncryptionCipherFacade(SharedPreferences prefs, FragmentActivity activity) {
        AndroidKeyStoreSecretKeyProvider secretKeyProvider = new AndroidKeyStoreSecretKeyProvider();
        this.cipher = new AndroidKeyStoreAsyncCipher(secretKeyProvider, activity);
        cipher.setDecryptFinishEventListener(decryptFinishEventListenerInFacade);
        cipher.setEncryptFinishEventListener(encryptFinishEventListenerInFacade);
        this.iv = new SharedPreferencesInitialVector(prefs);
    }

    @Override
    public void encrypt(String message, String key) throws CipherFacadeException {
        try {
            cipher.encrypt(message);
            iv.save(cipher.getIv(), key);
        } catch (CipherException e) {
            e.printStackTrace();
            throw new CipherFacadeException();
        }
    }

    @Override
    public void decrypt(String encryptedMessage, String key) throws CipherFacadeException {
        try {
            byte[] iv = this.iv.load(key);
            cipher.setIv(iv);
            cipher.decrypt(encryptedMessage);
        } catch (CipherException e) {
            e.printStackTrace();
            throw new CipherFacadeException();
        }
    }

    @Override
    public void setEncryptFinishEventListener(EncryptFinishEventListener encryptFinishEventListener) {
        this.encryptFinishEventListener=encryptFinishEventListener;
    }

    @Override
    public void setDecryptFinishEventListener(DecryptFinishEventListener decryptFinishEventListener) {//TODO: onAttach
        this.decryptFinishEventListener = decryptFinishEventListener;
    }

    AsyncCipher.DecryptFinishEventListener decryptFinishEventListenerInFacade = new AsyncCipher.DecryptFinishEventListener() {
        @Override
        public void onDecryptFinishEvent(String decryptedMessage) {
            if (decryptFinishEventListener != null)
                decryptFinishEventListener.onDecryptFinishEvent(decryptedMessage);
        }
    };

    AsyncCipher.EncryptFinishEventListener encryptFinishEventListenerInFacade = new AsyncCipher.EncryptFinishEventListener() {
        @Override
        public void onEncryptFinishEvent(String encryptedMessage) {
            if (encryptFinishEventListener != null)
                encryptFinishEventListener.onEncryptFinishEvent(encryptedMessage);
        }
    };

}
