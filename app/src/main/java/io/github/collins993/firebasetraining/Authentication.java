package io.github.collins993.firebasetraining;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Authentication extends AppCompatActivity {

    EditText countryCode, phoneNumber, enterOTPField;
    Button sendOTPBtn, verifyOTPBtn, resendOTPBtn;
    String userPhoneNumber, verificationId;
    PhoneAuthProvider.ForceResendingToken token;
    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        countryCode = findViewById(R.id.country_code);
        phoneNumber = findViewById(R.id.phone_number);
        enterOTPField = findViewById(R.id.enter_otp_field);
        sendOTPBtn = findViewById(R.id.send_otp);
        verifyOTPBtn = findViewById(R.id.verify);
        resendOTPBtn = findViewById(R.id.re_send_otp_btn);
        resendOTPBtn.setEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();

        sendOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (countryCode.getText().toString().isEmpty()) {

                    countryCode.setError("Required");
                    return;
                }

                if (phoneNumber.getText().toString().isEmpty()) {

                    phoneNumber.setError("Phone Number Required");
                    return;
                }

                userPhoneNumber = "+"+ countryCode.getText().toString() + phoneNumber.getText().toString();

                verifyPhoneNumber(userPhoneNumber);

                Toast.makeText(Authentication.this, userPhoneNumber, Toast.LENGTH_SHORT).show();
            }
        });

        resendOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyPhoneNumber(userPhoneNumber);
                resendOTPBtn.setEnabled(false);
            }
        });

        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (enterOTPField.getText().toString().isEmpty()) {

                    enterOTPField.setError("Enter OTP");
                    return;
                }

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, enterOTPField.getText().toString());
                authenticateUser(credential);

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                authenticateUser(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Toast.makeText(Authentication.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationId = s;
                token = forceResendingToken;

                countryCode.setVisibility(View.GONE);
                phoneNumber.setVisibility(View.GONE);
                sendOTPBtn.setVisibility(View.GONE);

                enterOTPField.setVisibility(View.VISIBLE);
                verifyOTPBtn.setVisibility(View.VISIBLE);
                resendOTPBtn.setVisibility(View.VISIBLE);

                resendOTPBtn.setEnabled(false);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);

                resendOTPBtn.setEnabled(true);
            }
        };


    }

    public void verifyPhoneNumber(String phoneNum) {

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setActivity(this)
                .setPhoneNumber(phoneNum)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void authenticateUser(PhoneAuthCredential credential) {

        FirebaseAuth.getInstance().getFirebaseAuthSettings().forceRecaptchaFlowForTesting(true);

        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                Toast.makeText(Authentication.this, "Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(Authentication.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            startActivity(new Intent(getApplicationContext(), MainActivity.class));

            finish();
        }
    }
}