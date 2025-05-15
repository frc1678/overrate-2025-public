package org.citruscircuits.overrate.ui

import com.ramcosta.composedestinations.annotation.NavGraph

/**
 * The main navigation graph for the app.
 *
 * @param start Whether the annotated destination is the start destination.
 */
@NavGraph
annotation class MainNavGraph(
    val start: Boolean = false
)
