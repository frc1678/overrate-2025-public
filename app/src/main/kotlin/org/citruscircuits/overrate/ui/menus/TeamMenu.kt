package org.citruscircuits.overrate.ui.menus

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Menu for team item options.
 *
 * @param expanded Whether the menu is expanded.
 * @param onDismissRequest Callback for dismissing the menu.
 * @param actions Actions to run when items are selected.
 */
@Composable
fun TeamMenu(expanded: Boolean, onDismissRequest: () -> Unit, actions: TeamMenuActions) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(text = { Text("Edit team") }, onClick = {
            actions.edit()
            onDismissRequest()
        }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) })
        DropdownMenuItem(text = { Text("Delete") }, onClick = {
            actions.delete()
            onDismissRequest()
        }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) })
    }
}

/**
 * Actions holder for the [TeamMenu].
 */
data class TeamMenuActions(val edit: () -> Unit, val delete: () -> Unit)
