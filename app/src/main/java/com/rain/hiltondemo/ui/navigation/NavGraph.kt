package com.rain.hiltondemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rain.hiltondemo.ui.screen.DetailScreen
import com.rain.hiltondemo.ui.screen.SearchScreen
import com.rain.hiltondemo.ui.screen.SplashScreen
import com.rain.hiltondemo.ui.viewmodel.PokemonViewModel

object Routes {
    const val SPLASH = "splash"
    const val SEARCH = "search"
    const val DETAIL = "detail/{pokemonId}"

    fun detailRoute(pokemonId: Int) = "detail/$pokemonId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: PokemonViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.SEARCH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = viewModel,
                onPokemonClick = { pokemonId ->
                    navController.navigate(Routes.detailRoute(pokemonId))
                }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("pokemonId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val pokemonId = backStackEntry.arguments?.getInt("pokemonId") ?: 0
            DetailScreen(
                pokemonId = pokemonId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
