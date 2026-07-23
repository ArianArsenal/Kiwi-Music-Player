//package com.example.kiwi.presentation.screens
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.shrinkVertically
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.kiwi.R
//import com.example.kiwi.presentation.viewmodel.SocialViewModel
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FollowingScreen(
//    onBackClick: () -> Unit,
//    viewModel: SocialViewModel = hiltViewModel()
//) {
//    val followedUsers by viewModel.followedUsers.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(stringResource(id = R.string.following), fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        if (followedUsers.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Icon(
//                        imageVector = Icons.Default.Person,
//                        contentDescription = null,
//                        modifier = Modifier.size(80.dp),
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        text = stringResource(id = R.string.empty_following_title),
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = stringResource(id = R.string.empty_following_desc),
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues),
//                contentPadding = PaddingValues(vertical = 8.dp)
//            ) {
//                items(
//                    items = followedUsers,
//                    key = { it.id }
//                ) { user ->
//                    var isVisible by remember { mutableStateOf(true) }
//
//                    AnimatedVisibility(
//                        visible = isVisible,
//                        exit = shrinkVertically(animationSpec = tween(300))
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 16.dp, vertical = 12.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Surface(
//                                modifier = Modifier.size(50.dp),
//                                shape = CircleShape,
//                                color = MaterialTheme.colorScheme.primaryContainer
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Person,
//                                    contentDescription = null,
//                                    modifier = Modifier.padding(12.dp),
//                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.width(16.dp))
//
//                            Column(modifier = Modifier.weight(1f)) {
//                                Text(
//                                    text = user.displayName,
//                                    style = MaterialTheme.typography.titleMedium,
//                                    fontWeight = FontWeight.Bold
//                                )
//                                Text(
//                                    text = stringResource(id = R.string.username_format, user.username),
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                )
//                            }
//
//                            OutlinedButton(
//                                onClick = {
//                                    isVisible = false
//                                    viewModel.unfollowUser(user.id)
//                                },
//                                shape = CircleShape,
//                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
//                            ) {
//                                Text(stringResource(id = R.string.unfollow))
//                            }
//                        }
//                    }
//                    HorizontalDivider(modifier = Modifier.padding(start = 82.dp))
//                }
//            }
//        }
//    }
//}