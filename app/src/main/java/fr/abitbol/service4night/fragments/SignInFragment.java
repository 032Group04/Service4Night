package fr.abitbol.service4night.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import fr.abitbol.service4night.R;
import fr.abitbol.service4night.databinding.FragmentSignInBinding;


public class SignInFragment extends Fragment implements OnCompleteListener<AuthResult> {
    private final String TAG= "Sign in fragment logging";
   FragmentSignInBinding binding;

    public SignInFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.signInPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (view.getId() == binding.signInPassword.getId() ){
                    binding.signInPasswordRequiresEditText.setVisibility(
                            binding.signInPassword.isFocused() ? View.VISIBLE : View.GONE
                    );

                }
            }
        });
        binding.signInButton.setOnClickListener(v ->{
            if (binding.signInPassword.getText().length() != 0){
                Log.i(TAG, "onViewCreated: password length > 0");
                if (binding.signInEmailEditText.getText().length() != 0){
                    Log.i(TAG, "onViewCreated: mail length > 0");
                    if (checkPassword() && checkMail()){
                        Log.i(TAG, "onViewCreated: password check passed");
                        registerUser();
                    }
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.empty_mail), Toast.LENGTH_SHORT).show();
                }

            }
            else{
                Toast.makeText(getContext(), getString(R.string.empty_password), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public boolean checkMail(){
        if (binding.signInEmailEditText.getText().toString().matches("[a-z0-9*\\-\\._]{1,30}@[a-z]{1,30}\\.[a-z]{2,20}")){
            Log.i(TAG, "checkMail: regex ok ");
            return true;
        }
        Log.i(TAG, "checkMail: regex not passed");
        Toast.makeText(getContext(), getString(R.string.mail_format_wrong), Toast.LENGTH_SHORT).show();
        return false;
    }
    public boolean checkPassword(){
        if (binding.signInPassword.getText().equals(binding.signInPasswordConfirm.getText())){
            Log.i(TAG, "checkPassword: confirm ok");
            if (binding.signInPassword.getText().toString().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#/$.%&])(?=.{8,25})")){
                Log.i(TAG, "checkPassword: regex passed");
                return true;
            }
            else{
                Log.e(TAG, "checkPassword: wrong format" );
                Toast.makeText(getContext(), getString(R.string.password_format_wrong), Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Log.e(TAG, "checkPassword: password not confirmed");
            Toast.makeText(getContext(), getString(R.string.password_unconfirmed), Toast.LENGTH_SHORT).show();

        }
        return false;
    }

    public void registerUser(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null){
            Toast.makeText(getContext(), getString(R.string.already_looged_in), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(SignInFragment.this).popBackStack();
        }
        else{
            auth.createUserWithEmailAndPassword(binding.signInEmailEditText.getText().toString(),
                    binding.signInPassword.getText().toString())
                    .addOnCompleteListener(this);

        }
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()){
            if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
                Log.i(TAG, "registerUser: success , user :" + FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }

        }
        else{
            if (task.getException() != null) {
                Log.e(TAG, "onActivityResult: connection failed :" + task.getException().getMessage());
                Toast.makeText(getContext(), getString(R.string.sign_in_failed) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            else{
                Log.e(TAG, "onActivityResult: connection failed");
                Toast.makeText(getContext(), getString(R.string.sign_in_failed) , Toast.LENGTH_SHORT).show();
            }

        }
    }
}