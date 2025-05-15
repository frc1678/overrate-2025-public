package org.citruscircuits.overrate.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import org.citruscircuits.overrate.data.MainListItem
import kotlin.math.abs
import kotlin.math.sign

/** Converts the given [this] to a list containing only the team number present in [this] for ranking purposes. Dividers are thrown out. */
fun List<MainListItem>.toTeamNumberList() : List<String> {
    val returnList = emptyList<String>().toMutableList()
    for (item in this.filterIsInstance<MainListItem.Team>()) {
        returnList += item.number
    }
    return returnList.toList()
}

/**
 * A card which displays the difference in ranking for a given item
 * @param itemRankingDifference Total difference between the two lists
 * */
@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun RankingDifferenceCard(
    itemRankingDifference: Int,
    itemDeleted: Boolean,
    modifier: Modifier = Modifier
) {
    val itemRankingColor = if (itemDeleted) {Color.Black} else when (sign(itemRankingDifference.toDouble())) {
        1.0 -> Color.Green
        0.0 -> Color.Transparent
        else -> Color.Red
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        // animate value going up or down
        AnimatedContent(
            targetState = itemRankingDifference,
            label = "",
            transitionSpec = {
                if (targetState > initialState) {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                        slideOutVertically { height -> height } + fadeOut()
                } else {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
                }.using(SizeTransform(clip = false))
            }
        ) {
            if (itemRankingDifference != 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (itemRankingColor == Color.Black) "" else "${abs(itemRankingDifference)}",
                        color = itemRankingColor,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = when (itemRankingColor) {
                                Color.Green -> Icons.Default.ArrowUpward
                                Color.Red -> Icons.Default.ArrowDownward
                                Color.Black -> Icons.Default.Close
                                else -> Icons.Default.Circle
                            },
                            contentDescription = null,
                            tint = itemRankingColor
                        )
                    }
                }
            }
        }
    }
}
