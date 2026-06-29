package com.example.spotted.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // ✅ Import aggiunto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotted.data.view.LoginViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    innerPadding: PaddingValues,
    navigate: () -> Unit,
    navigateToSignup: () -> Unit // ✅ 1. Nuovo parametro per la navigazione
) {
    var email by remember { mutableStateOf(String()) }
    var password by remember { mutableStateOf(String()) }
    val viewModel: LoginViewModel = koinViewModel()
    var localError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    // Osserva l'utente e naviga quando il login ha successo
    val user by viewModel.user.collectAsState()
    LaunchedEffect(user) {
        if (user != null) navigate()
    }

    // Osserva l'errore per mostrarlo all'utente
    val error by viewModel.error.collectAsState()

    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append("Spotted")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append("Unibo")
                        }
                    },
                    fontSize = 40.sp
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                        localError = null
                    },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("nome.cognomeNN@studio.unibo.it") },
                    isError = emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false
                        localError = null
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError,
                )




                // Gestione errore: locale ha priorità, altrimenti controlla risposta Supabase
                val displayError = localError ?: error
                if (displayError != null) {
                    Text(
                        text = displayError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        localError = null
                        emailError = false
                        passwordError = false

                        when {
                            email.isBlank() && password.isBlank() -> {
                                emailError = true
                                passwordError = true
                                localError = "Inserisci email e password"
                            }
                            email.isBlank() -> {
                                emailError = true
                                localError = "Inserisci la tua email"
                            }
                            password.isBlank() -> {
                                passwordError = true
                                localError = "Inserisci la tua password"
                            }
                            else -> viewModel.login(email, password)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(if (isLoading) "Caricamento..." else "Accedi")
                }

                // ✅ 2. Aggiunto TextButton per andare alla schermata di Registrazione
                TextButton(
                    onClick = navigateToSignup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Non hai un account? Registrati",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}