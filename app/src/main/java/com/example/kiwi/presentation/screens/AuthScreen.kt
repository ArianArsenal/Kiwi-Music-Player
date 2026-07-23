package com.example.kiwi.presentation.screens

import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiwi.R
import com.example.kiwi.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val authError by viewModel.authError.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onAuthSuccess()
    }

    var isLoginMode by remember { mutableStateOf(true) }

    // Unified variable for the first input field
    var emailOrUsername by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Dynamic validation: Login needs any valid length, Signup requires strict email pattern
    val isIdentifierValid = if (isLoginMode) {
        emailOrUsername.length >= 3
    } else {
        emailOrUsername.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()
    }

    val isPasswordValid = password.length >= 6
    val isUsernameValid = username.length >= 3

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.background
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.kiwi_logo_transparent),
                contentDescription = stringResource(id = R.string.content_desc_app_logo),
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .padding(bottom = 24.dp)
            )

            AnimatedContent(
                targetState = isLoginMode,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "titleAnimation"
            ) { isLogin ->
                Text(
                    text = if (isLogin) stringResource(id = R.string.welcome_back) else stringResource(id = R.string.create_account),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.enter_details),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = !isLoginMode,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(tween(300))
            ) {
                Column {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            viewModel.clearError()
                        },
                        label = { Text(stringResource(id = R.string.username)) },
                        isError = username.isNotBlank() && !isUsernameValid,
                        supportingText = {
                            if (username.isNotBlank() && !isUsernameValid) {
                                Text(stringResource(id = R.string.error_invalid_username), color = MaterialTheme.colorScheme.error)
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Unified Input Field for Email (Signup) or Email/Username (Login)
            OutlinedTextField(
                value = emailOrUsername,
                onValueChange = {
                    emailOrUsername = it
                    viewModel.clearError()
                },
                label = {
                    Text(
                        if (isLoginMode) stringResource(id = R.string.email_or_username)
                        else stringResource(id = R.string.email)
                    )
                },
                isError = emailOrUsername.isNotBlank() && !isIdentifierValid,
                supportingText = {
                    if (emailOrUsername.isNotBlank() && !isIdentifierValid) {
                        val errorMsg = if (isLoginMode) stringResource(id = R.string.error_invalid_username) else stringResource(id = R.string.error_invalid_email)
                        Text(errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isLoginMode) KeyboardType.Text else KeyboardType.Email
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearError()
                },
                label = { Text(stringResource(id = R.string.password)) },
                isError = password.isNotBlank() && !isPasswordValid,
                supportingText = {
                    if (password.isNotBlank() && !isPasswordValid) {
                        Text(stringResource(id = R.string.error_invalid_password), color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            if (authError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = authError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                // Passing the unified field to the ViewModel
                onClick = { viewModel.loginOrSignUp(emailOrUsername, password, username, isLoginMode) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isIdentifierValid && isPasswordValid && (isLoginMode || isUsernameValid)
            ) {
                Text(
                    text = if (isLoginMode) stringResource(id = R.string.log_in) else stringResource(id = R.string.sign_up),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                isLoginMode = !isLoginMode
                viewModel.clearError()
            }) {
                Text(
                    text = if (isLoginMode) stringResource(id = R.string.no_account) else stringResource(id = R.string.have_account),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}