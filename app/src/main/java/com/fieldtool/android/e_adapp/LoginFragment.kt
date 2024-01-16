package com.fieldtool.android.e_adapp

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fieldtool.android.e_adapp.databinding.FragmentLoginBinding
import com.fieldtool.android.e_adapp.util.toast
import java.util.concurrent.Executor

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var biometricPrompt: BiometricPrompt


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSpannableText()
        binding.apply {
            btnLogin.setOnClickListener { loginUser() }
            icFingerprint.setOnClickListener { authenticateWithBiometrics() }
        }
    }



    private fun loginUser() {
        val input = binding.phoneNumberTxt.text.toString().trim().replace(",", "")
        val pin = binding.passwordTxt.text.toString().trim()
        when {
            input.isEmpty() -> toast("Enter Phone Number/Email")
            pin.isEmpty() -> toast("Enter PIN")
            isPhoneNumber(input) && input.length != 10 -> toast("Please enter a valid phone number of 10 digits.")
            isEmail(input) && !isValidEmail(input) -> toast("Please enter a valid email address.")
            else -> toast("Please enter a valid phone number or email address.")
        }
    }

    private fun isPhoneNumber(input: String): Boolean {
        return input.all { it.isDigit() }
    }

    private fun isEmail(input: String): Boolean {
        return input.contains("@")
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun initSpannableText() {
        val spannableString = SpannableString("Don't have an Account? Create Account")
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.smallTextColor)),
            0, 24, 0
        )
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.tint_green)),
            22, spannableString.length, 0
        )
        binding.signUp.text = spannableString
    }


    private fun authenticateWithBiometrics() {
        val cryptoHelper = CryptoHelper(this@LoginFragment.requireContext())
//
//        if (!cryptoHelper.checkOneBiometricMustBeEnrolled()) {
//            toast("No biometric data enrolled. Please enroll at least one biometric credential.")
//            return
//        }

        val executor: Executor = ContextCompat.getMainExecutor(requireActivity())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_LOCKOUT -> toast("Too many wrong attempts. Try again later.")
                        BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {}
                        else -> toast("$errString")
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    toast("Fingerprint Authentication Successful")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    toast("Fingerprint Authentication Failed")
                }
            })

        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("Biometric Login")
            setNegativeButtonText("Cancel")
            setConfirmationRequired(true)
            setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            biometricPrompt.authenticate(this.build())
        }
    }

}