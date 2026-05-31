package com.cheezu.kantongku.auth;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import java.security.SecureRandom;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

public class SSOAuthService {
    private Context context;
    private CredentialManager credentialManager;

    public SSOAuthService(Context context) {
        this.context = context;
        this.credentialManager = CredentialManager.create(context);
    }

    public void signInWithGoogle() {
        // Generate a secure random nonce automatically
        SecureRandom random = new SecureRandom();
        byte[] nonceBytes = new byte[32];
        random.nextBytes(nonceBytes);
        String nonce = Base64.encodeToString(nonceBytes, Base64.NO_WRAP);

        // Create the Google ID request using the generated nonce
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("564737094443-6e9j5f8ql27achom1jtq0jiukjbc8ass.apps.googleusercontent.com")
                .setNonce(nonce)
                .build();

        // Create the credential request
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // TODO: Implement the actual sign in flow
        // This is a placeholder for the sign in implementation
    }

    public GoogleIdTokenCredential parseGoogleIdToken(Bundle data) {
        return GoogleIdTokenCredential.createFrom(data);
    }
}