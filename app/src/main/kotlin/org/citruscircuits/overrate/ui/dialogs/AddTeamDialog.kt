package org.citruscircuits.overrate.ui.dialogs

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import org.citruscircuits.overrate.MainActivityViewModel
import org.citruscircuits.overrate.data.MainListItem
import org.citruscircuits.overrate.ui.MainNavGraph
import org.citruscircuits.overrate.ui.components.toTeamNumberList

/**
 * Dialog for adding a team to the main list.
 */
@SuppressLint("SuspiciousIndentation")
@MainNavGraph
@Destination(style = DestinationStyle.Dialog::class)
@Composable
fun AddTeamDialog(navigator: DestinationsNavigator, viewModel: MainActivityViewModel) {
    // get app state
    val appState by viewModel.appState.collectAsStateWithLifecycle()
    // all teams currently imported
    val currentTeams = appState.list.toTeamNumberList().toMutableList()
    // current team number input
    var teamNumber by rememberSaveable { mutableStateOf("") }
    // current comment input
    var comment by rememberSaveable { mutableStateOf("") }
    // when 'add all teams' option is enabled
    var addAllTeams by remember { mutableStateOf(false) }
    // multiple Teams to Add
    var addedTeams by remember { mutableStateOf(setOf("")) }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                // scrollable
                .verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // title
            Text("Add team", style = MaterialTheme.typography.headlineMedium)
            // team number input field
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // input field
                OutlinedTextField(value = teamNumber,
                    onValueChange = { teamNumber = it },
                    label = { Text("Enter team number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // show results if something is typed
                if (teamNumber.isNotEmpty()) {
                    // show results in card
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        // sort/filter search results
                        appState.importedTeams.filter { teamNumber in it }.filterNot { teamNumber == it }
                            .sortedBy { it.toIntOrNull() }.take(3).forEach { team ->
                                ListItem(headlineContent = { Text(team) },
                                    modifier = Modifier.clickable { teamNumber = team })
                                HorizontalDivider()
                            }
                    }
                }
            }
            // comment input field
            OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Enter comment") })
            // cancel/add buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // checks whether or not all teams have been added
                if (currentTeams != appState.importedTeams) {
                    // add all teams checkbox
                    Checkbox(
                        checked = addAllTeams,
                        onCheckedChange = { addAllTeams = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3CD52D))
                    )
                    Text("Add all teams", fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                // cancel button
                OutlinedButton(onClick = navigator::navigateUp) {
                    Text("Cancel", color = Color(0xFF3CD52D))
                }
                // add button
                Button(onClick = {
                    if (teamNumber != "") {
                        viewModel.addTeam(teamNumber, comment)
                    }
                    navigator.navigateUp()
                    // if addedTeams is not empty (an set containing empty string)
                    if (!addedTeams.contains("")) addedTeams.forEach { viewModel.addTeam(it) }
                },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3CD52D))
                ) {
                    Text("Add")
                }
            }
            if (appState.importedTeams.isEmpty()) {
                Text(
                    "You haven't imported any teams yet!", fontSize = 12.sp, textAlign = TextAlign.Center
                )
            } else if (currentTeams.toSet() == appState.importedTeams) {
                Text(
                    "All of the teams have been added!", fontSize = 12.sp, textAlign = TextAlign.Center
                )
            }
        }
        val availableTeams = appState.importedTeams.filterNot { it in currentTeams }
        if (availableTeams.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                availableTeams.forEach { team ->
                    var isPressed by remember { mutableStateOf(false) }
                    Button(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(1000.dp)
                            .padding(horizontal = 20.dp)
                            .padding(vertical = 5.dp)
                            .align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPressed) Color(0xFF3CD52D) else Color.LightGray),
                        onClick = { isPressed = !isPressed },
                    ) {
                        LaunchedEffect(addAllTeams) {
                            isPressed = addAllTeams
                            addedTeams = if (addAllTeams) availableTeams.toSet() else emptySet()
                        }
                        LaunchedEffect(isPressed) {
                            if (isPressed) {
                                addedTeams += team
                            } else {
                                addedTeams =
                                    addedTeams.filter { it != team }.toSet()
                            }
                        }
                        Text(
                            team, fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }
    }
}
