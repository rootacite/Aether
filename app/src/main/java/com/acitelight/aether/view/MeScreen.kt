package com.acitelight.aether.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acitelight.aether.viewModel.MeScreenViewModel

@Composable
fun MeScreen(meScreenViewModel: MeScreenViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<MeScreenViewModel>()) {
    var username by meScreenViewModel.username
    var privateKey by meScreenViewModel.privateKey
    var url by meScreenViewModel.url
    var cert by meScreenViewModel.cert

    val uss by meScreenViewModel.uss.collectAsState(initial = false)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Card component for a clean, contained UI block
        item{
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Account Setting",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.Start)
                    )

                    // Username input field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Username")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Private key input field
                    OutlinedTextField(
                        value = privateKey,
                        onValueChange = { privateKey = it },
                        label = { Text("Key") },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = "Key")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Button
                    Button(
                        onClick = {
                            meScreenViewModel.updateAccount(username, privateKey)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = privateKey != "******"
                    ) {
                        Text("Save")
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Server Setting",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Username input field
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Url") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = "Url")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(Modifier.align(Alignment.Start)) {
                        Checkbox(
                            checked = uss,
                            onCheckedChange = { isChecked ->
                                meScreenViewModel.onUseSelfSignedCheckedChange(isChecked)
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Use Self-Signed Cert",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Private key input field
                    if (uss)
                        OutlinedTextField(
                            value = cert,
                            onValueChange = { cert = it },
                            label = { Text("Cert") },
                            singleLine = false,
                            maxLines = 40,
                            minLines = 20,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontSize = 8.sp
                            )
                        )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Button
                    Row{
                        Button(
                            onClick = {
                                meScreenViewModel.updateServer(url, cert)
                            },
                            modifier = Modifier.weight(0.5f).padding(8.dp)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

