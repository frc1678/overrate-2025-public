package org.citruscircuits.overrate.ui.pages

import android.annotation.SuppressLint
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.citruscircuits.overrate.AppState
import org.citruscircuits.overrate.MainActivityViewModel
import org.citruscircuits.overrate.data.MainListItem
import org.citruscircuits.overrate.ui.MainNavGraph
import org.citruscircuits.overrate.ui.animation.Transitions
import org.citruscircuits.overrate.ui.components.Counter
import org.citruscircuits.overrate.ui.components.RankingDifferenceCard
import org.citruscircuits.overrate.ui.components.toTeamNumberList
import org.citruscircuits.overrate.ui.destinations.AddTeamDialogDestination
import org.citruscircuits.overrate.ui.destinations.ClearListDialogDestination
import org.citruscircuits.overrate.ui.destinations.EditDividerLabelDialogDestination
import org.citruscircuits.overrate.ui.destinations.EditTeamDialogDestination
import org.citruscircuits.overrate.ui.destinations.ImportTeamsPageDestination
import org.citruscircuits.overrate.ui.destinations.MainPageDestination
import org.citruscircuits.overrate.ui.menus.AddMenu
import org.citruscircuits.overrate.ui.menus.AddMenuActions
import org.citruscircuits.overrate.ui.menus.DividerMenu
import org.citruscircuits.overrate.ui.menus.DividerMenuActions
import org.citruscircuits.overrate.ui.menus.TeamMenu
import org.citruscircuits.overrate.ui.menus.TeamMenuActions
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import kotlin.math.max

/**
 * Page containing the main list.
 */
@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@MainNavGraph(start = true)
@Destination(style = MainPageTransitions::class)
@Composable
fun MainPage(navigator: DestinationsNavigator, viewModel: MainActivityViewModel) {
    // get app state
    val appState by viewModel.appState.collectAsStateWithLifecycle()
    // parallel app state
    val parallelAppState by viewModel.parallelAppState.collectAsStateWithLifecycle()
    var parallelLeaderboard by remember { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("OverRate") },
            // actions at top right
            actions = {
                if (!parallelLeaderboard) {
                    // turn on parallel leaderboards mode
                    IconButton(
                        onClick = {
                            parallelLeaderboard = true
                            viewModel.updateAppState(true)
                        }
                    ) {
                        Icon(Icons.Default.VerticalSplit, contentDescription = null)
                    }
                } else {
                    // confirm changes
                    IconButton(
                        onClick = {
                            parallelLeaderboard = false
                            viewModel.updateAppState(false)
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Green)
                    }
                    // reject changes
                    IconButton(
                        onClick = { parallelLeaderboard = false }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                    }
                }
                Box {
                    // whether the menu is shown
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    // add button
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                    // menu for add button
                    AddMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        actions = AddMenuActions(addTeam = { navigator.navigate(AddTeamDialogDestination) },
                            addDivider = { viewModel.addDivider() })
                    )
                }
                // import button
                IconButton(onClick = { navigator.navigate(ImportTeamsPageDestination) }) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                }
                // clear button
                IconButton(onClick = { navigator.navigate(ClearListDialogDestination) }) {
                    Icon(Icons.Default.ClearAll, contentDescription = null)
                }
            })
    }) { padding ->
        // state of the main list
        var lazyListState = rememberLazyListState()
        // update app state when list is reordered
        var reorderableLazyColumnState = rememberReorderableLazyColumnState(lazyListState = lazyListState) { from, to ->
            viewModel.appState.value =
                appState.copy(list = appState.list.toMutableList().also { it.add(to.index, it.removeAt(from.index)) })
        }
        // state of the parallel list
        var parallelLazyListState = rememberLazyListState()
        //parallel of above
        var parallelReorderableLazyColumnState =
            rememberReorderableLazyColumnState(lazyListState = parallelLazyListState) { from, to ->
                viewModel.parallelAppState.value =
                    parallelAppState.copy(
                        list = parallelAppState.list.toMutableList().also { it.add(to.index, it.removeAt(from.index)) })
            }
        if (parallelLeaderboard) {
            lazyListState = parallelLazyListState
            reorderableLazyColumnState = parallelReorderableLazyColumnState
        }
        Row(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            // main list
            Leaderboard(
                viewModel,
                lazyListState,
                Pair(appState, parallelAppState),
                reorderableLazyColumnState,
                navigator,
                { item -> viewModel.updateItem(item, false) },
                { item -> viewModel.deleteItem(item, false) },
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(padding),
                !parallelLeaderboard,
                !parallelLeaderboard
            )
            if (parallelLeaderboard) {
                // state of the parallel list
                parallelLazyListState = rememberLazyListState()
                //parallel of above
                parallelReorderableLazyColumnState =
                    rememberReorderableLazyColumnState(lazyListState = parallelLazyListState) { from, to ->
                        viewModel.parallelAppState.value =
                            parallelAppState.copy(
                                list = parallelAppState.list.toMutableList()
                                    .also { it.add(to.index, it.removeAt(from.index)) })
                    }
                // main list 2
                Leaderboard(
                    viewModel,
                    parallelLazyListState,
                    Pair(parallelAppState, parallelAppState),
                    parallelReorderableLazyColumnState,
                    navigator,
                    { item -> viewModel.updateItem(item, true) },
                    { item -> viewModel.deleteItem(item, true) },
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(padding),
                    false
                )
            }
        }
        // check if there are items
        if (appState.list.isEmpty()) {
            // no items, show placeholder
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // help text
                Text("Nothing here yet! Add some teams to get started.", textAlign = TextAlign.Center)
                // button to add a team
                OutlinedButton(onClick = { navigator.navigate(AddTeamDialogDestination) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add a team")
                }
            }
        }
    }
}

/**
 * An individual instance of a leaderboard shown in the app
 * @param lazyListState The current state of this particular [Leaderboard]'s lazy list
 * @param appStates A pair containing the [AppState] being displayed, and then the [AppState] being compared to
 * @param reorderableLazyColumnState The current reorderable state of this particular [Leaderboard]'s lazy list
 * @param updateItem A lambda which updates the passed in item
 * @param deleteItem A lambda which deletes the passed in item
 * @param notesVisible When the individual team notes should be visible (invisible during parallel leaderboards mode to save space)
 * @param reorderable When the leaderboard's items should be active and interactable
 * */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Leaderboard(
    viewModel: MainActivityViewModel,
    lazyListState: LazyListState,
    appStates: Pair<AppState, AppState>,
    reorderableLazyColumnState: ReorderableLazyListState,
    navigator: DestinationsNavigator,
    updateItem: (MainListItem) -> Unit,
    deleteItem: (MainListItem) -> Unit,
    modifier: Modifier = Modifier,
    notesVisible: Boolean = true,
    reorderable: Boolean = true
) {
    val thisAppState = appStates.first
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        // show items
        items(thisAppState.list, key = { it.id }) { item ->
            // allow reordering
            ReorderableItem(reorderableLazyListState = reorderableLazyColumnState, key = item.id) {
                // check if the item is a team or a divider
                when (item) {
                    is MainListItem.Team -> {
                        viewModel.createdTeamEntries =
                            viewModel.createdTeamEntries.filter { it.isNotEmpty() }.toMutableList()
                        viewModel.createdTeamEntries = viewModel.createdTeamEntries.map { it.trim() }.toMutableList()
                        Box {
                            // whether the menu is expanded
                            var expanded by rememberSaveable { mutableStateOf(false) }
                            // team card
                            Card(
                                colors = if (reorderable) {
                                    CardDefaults.cardColors(
                                        containerColor = Color(0xFF3CD52D)
                                    )
                                } else {
                                    CardDefaults.cardColors()
                                }
                            ) {
                                // stack horizontally
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .combinedClickable(onLongClick = { expanded = reorderable }) {}) {
                                    // drag handle
                                    IconButton(
                                        onClick = {}, modifier = if (reorderable) {
                                            Modifier.draggableHandle()
                                        } else {
                                            Modifier
                                        }
                                    ) {
                                        Icon(
                                            if (reorderable) {
                                                Icons.Default.DragHandle
                                            } else {
                                                Icons.Default.Lock
                                            }, contentDescription = null
                                        )
                                    }
                                    // team number
                                    Box(modifier = Modifier.weight(item.number.length * 3f + 0.01f)) {
                                        Text(item.number, maxLines = 2, overflow = TextOverflow.Clip)
                                    }
                                    // comment
                                    Box(
                                        Modifier.weight(max(0.01f, 20f - item.number.length * 1f))
                                            .fillMaxWidth() // Ensures it takes available width
                                    ) {
                                        Text(
                                            item.comment,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = Int.MAX_VALUE,
                                            overflow = TextOverflow.Clip
                                        )
                                    }

                                    // move items to opposite sides
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (notesVisible) {
                                        // team rating counter
                                        Counter(value = item.rating, setValue = { updateItem(item.copy(rating = it)) })
                                    } else {
                                        val itemRankingDifference =
                                            (thisAppState.list.toTeamNumberList().indexOf(item.number) -
                                                appStates.second.list.toTeamNumberList().indexOf(item.number))
                                        val itemDeleted = (item !in appStates.second.list)
                                        // run the ranking difference card
                                        RankingDifferenceCard(
                                            itemRankingDifference = itemRankingDifference,
                                            itemDeleted = itemDeleted
                                        )
                                    }
                                }
                            }
                            // menu for team actions
                            TeamMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                actions = TeamMenuActions(edit = {
                                    navigator.navigate(EditTeamDialogDestination(id = item.id))
                                }, delete = { deleteItem(item) })
                            )
                        }
                    }

                    is MainListItem.Divider -> {
                        Box {
                            // whether the menu is expanded
                            var expanded by rememberSaveable { mutableStateOf(false) }
                            // divider card
                            Card {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .combinedClickable(onLongClick = { expanded = reorderable }) {}
                                        .fillMaxWidth()) {
                                    // drag handle
                                    IconButton(
                                        onClick = {}, modifier = if (reorderable) {
                                            Modifier.draggableHandle()
                                        } else {
                                            Modifier
                                        }
                                    ) {
                                        Icon(
                                            if (reorderable) {
                                                Icons.Default.DragHandle
                                            } else {
                                                Icons.Default.Lock
                                            }, contentDescription = null
                                        )
                                    }
                                    // label
                                    Text(item.label)
                                }
                            }
                            // menu for divider actions
                            DividerMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                actions = DividerMenuActions(rename = {
                                    navigator.navigate(EditDividerLabelDialogDestination(id = item.id))
                                }, delete = { deleteItem(item) })
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Transitions for [MainPage].
 */
object MainPageTransitions : Transitions(destination = MainPageDestination, enter = { fadeIn() }, exit = { fadeOut() })
