package org.citruscircuits.overrate

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.citruscircuits.overrate.data.MainListItem
import org.citruscircuits.overrate.data.configFile

/**
 * [ViewModel] for [MainActivity].
 */
class MainActivityViewModel : ViewModel() {
    /**
     * The state of the app.
     */
    val appState = MutableStateFlow(AppState(list = emptyList(), importedTeams = emptySet()))

    /**
     * For parallel leaderboard mode
     */
    val parallelAppState = MutableStateFlow(AppState(list = emptyList(), importedTeams = emptySet()))

    /**
     * Saves the team number for the entries to be rated
     */
    var createdTeamEntries = mutableListOf<String>()

    /**
     * Loads the app state and starts watching for changes.
     */
    fun load(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            // read state from file
            appState.value =
                if (context.configFile.exists()) {
                    Json.decodeFromString(context.configFile.readText())
                } else {
                    AppState(emptyList(), emptySet())
                }
            // write to file on every change in state
            viewModelScope.launch(Dispatchers.IO) {
                appState.collect { state ->
                    Log.i("MainActivityViewModel", "App state updated: $state")
                    context.configFile.writeText(Json.encodeToString(state))
                }
            }
        }
    }

    /**
     * Adds a team item with the given [number].
     *
     * @param number The team number.
     */
    fun addTeam(number: String, comment: String = "") {
        appState.value = appState.value.copy(
            list = appState.value.list + MainListItem.Team(number = number, rating = 0, comment = comment)
        )
    }

    /** Sets [appState] to [parallelAppState] or vice versa
     * @param towardsParallel Selects which appState is being changed
     * */
    fun updateAppState(towardsParallel: Boolean) {
        if (towardsParallel) {
            parallelAppState.value = appState.value
        } else {
            appState.value = parallelAppState.value
        }
    }

    /**
     * Adds a divider with the given [label].
     *
     * @param label The label of the divider.
     */
    fun addDivider(label: String = "Divider") {
        appState.value =
            appState.value.copy(list = mutableListOf(MainListItem.Divider(label = label)) + appState.value.list)
    }

    /**
     * Adds a collection of team numbers to the set of imported teams.
     *
     * @param teams The team numbers to import.
     */
    fun importTeams(teams: Collection<String>) {
        appState.value =
            appState.value.copy(importedTeams = appState.value.importedTeams + teams)
    }

    /**
     * Updates an existing item with a matching ID in the app state with [item].
     */
    fun updateItem(item: MainListItem, appStateParallel: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (appStateParallel) {
                parallelAppState.value =
                    AppState(
                        list = parallelAppState.value.list.map { if (it.id == item.id) item else it },
                        importedTeams = parallelAppState.value.importedTeams
                    )
            } else {
                appState.value =
                    AppState(
                        list = appState.value.list.map { if (it.id == item.id) item else it },
                        importedTeams = appState.value.importedTeams
                    )
            }
        }
    }

    /**
     * Deletes an item with a matching ID in the app state.
     */
    fun deleteItem(item: MainListItem, appStateParallel: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (appStateParallel) {
                parallelAppState.value =
                    AppState(
                        list = parallelAppState.value.list.filterNot { it.id == item.id },
                        importedTeams = parallelAppState.value.importedTeams
                    )
            } else {
                appState.value =
                    AppState(
                        list = appState.value.list.filterNot { it.id == item.id },
                        importedTeams = appState.value.importedTeams
                    )
            }
        }
    }
}
