package com.acitelight.aether.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.acitelight.aether.Global
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.RecentManager
import com.acitelight.aether.viewModel.HomeScreenViewModel

@Composable
fun HomeScreen(homeScreenViewModel: HomeScreenViewModel = hiltViewModel(), navController: NavController)
{
    if(Global.loggedIn)
        homeScreenViewModel.Init()

    LazyColumn(modifier = Modifier.fillMaxWidth())
    {
        item()
        {
            Column {
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp).align(Alignment.Start)
                )

                HorizontalDivider(Modifier.padding(8.dp), 2.dp, DividerDefaults.color)

                for(i in RecentManager.recent)
                {
                    MiniVideoCard(
                        modifier = Modifier
                            .padding(horizontal = 12.dp),
                        i,
                        {
                            Global.sameClassVideos = RecentManager.recent
                            val route = "video_player_route/${ "${i.klass}/${i.id}".toHex() }"
                            navController.navigate(route)
                        }, homeScreenViewModel.imageLoader!!)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp).alpha(0.25f), 1.dp, DividerDefaults.color)
                }
            }
        }
    }

}