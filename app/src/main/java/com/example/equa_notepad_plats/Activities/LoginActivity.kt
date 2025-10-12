package com.example.equa_notepad_plats.Activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.theme.AppTheme
import com.example.equa_notepad_plats.view_models.LoginViewModel

class LoginActivity : ComponentActivity() {

    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        credentialManager = CredentialManager.create(this)

        setContent {
            AppTheme(darkTheme = isSystemInDarkTheme()) {
                Scaffold() { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSignInClick = { handleSignIn() },
                        onSignUpClick = { handleSignUp() },
                        credentialManager = credentialManager,
                    )
                }
            }
        }
    }

    private fun handleSignIn() {
        val viewModel = LoginViewModel()
        viewModel.signInWithGoogle(
            credentialManager = credentialManager,
            context = this,
            onSuccess = { email, name ->
                Log.d("LoginActivity", "Sign in successful: $email")
                navigateToHome(email, name)
            },
            onError = { error ->
                Log.e("LoginActivity", "Sign in error: $error")
            }
        )
    }

    private fun handleSignUp() {
        val viewModel = LoginViewModel()
        viewModel.signUpWithGoogle(
            credentialManager = credentialManager,
            context = this,
            onSuccess = { email, name ->
                Log.d("LoginActivity", "Sign up successful: $email")
                navigateToHome(email, name)
            },
            onError = { error ->
                Log.e("LoginActivity", "Sign up error: $error")
            }
        )
    }

    private fun navigateToHome(email: String, name: String) {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("name", name)
        })
        finish()
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    credentialManager: CredentialManager,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Deltime",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Acceder con Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = ButtonDefaults.outlinedButtonBorder,
            enabled = !uiState.isLoading
        ) {
            Text(
                text = "Registrarse con Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = uiState.error,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AppTheme(darkTheme = false) {
        LoginScreen(
            onSignInClick = {},
            onSignUpClick = {},
            credentialManager = CredentialManager.create(LocalContext.current),
        )
    }
}
@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        LoginScreen(
            onSignInClick = {},
            onSignUpClick = {},
            credentialManager = CredentialManager.create(LocalContext.current),
        )
    }
}