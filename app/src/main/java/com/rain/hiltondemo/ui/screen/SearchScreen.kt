package com.rain.hiltondemo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rain.hiltondemo.data.model.PokemonSpecies
import com.rain.hiltondemo.ui.viewmodel.PokemonViewModel

/**
 * Maps Pokémon color names from the API to Compose Color values.
 */
private fun pokemonColorToCompose(colorName: String?): Color {
    return when (colorName?.lowercase()) {
        "black" -> Color(0xFF333333)
        "blue" -> Color(0xFF3B4CCA)
        "brown" -> Color(0xFFB1736C)
        "gray" -> Color(0xFFA8A8A8)
        "green" -> Color(0xFF4CAF50)
        "pink" -> Color(0xFFF8BBD0)
        "purple" -> Color(0xFF9C27B0)
        "red" -> Color(0xFFF44336)
        "white" -> Color(0xFFF5F5F5)
        "yellow" -> Color(0xFFFFEB3B)
        else -> Color(0xFFE0E0E0)
    }
}

@Composable
fun SearchScreen(
    viewModel: PokemonViewModel,
    onPokemonClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Detect scroll to bottom for pagination
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.hasMore && !uiState.isLoadMore) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Pokémon Search",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search input + button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter Pokémon name...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.search()
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.search()
                    },
                    enabled = uiState.query.trim().isNotEmpty() && !uiState.isLoading,
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content area
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Initial empty state
                    !uiState.isLoading && uiState.species.isEmpty() && uiState.error == null -> {
                        Text(
                            text = "Search for Pokémon species above",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }

                    // Loading indicator
                    uiState.isLoading && !uiState.isLoadMore -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Error message
                    uiState.error != null -> {
                        Text(
                            text = "Error: ${uiState.error}",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                    }

                    // Results list
                    uiState.species.isNotEmpty() -> {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.species, key = { it.name }) { species ->
                                SpeciesCard(
                                    species = species,
                                    onPokemonClick = onPokemonClick
                                )
                            }

                            // Loading more indicator
                            if (uiState.isLoadMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    }
                                }
                            }

                            // End of results
                            if (!uiState.hasMore && uiState.species.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "— End of results —",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeciesCard(
    species: PokemonSpecies,
    onPokemonClick: (Int) -> Unit
) {
    val bgColor = pokemonColorToCompose(species.pokemon_v2_pokemoncolor?.name)
    val textColor = if (species.pokemon_v2_pokemoncolor?.name?.lowercase() in listOf("black", "blue", "purple", "brown", "gray")) {
        Color.White
    } else {
        Color.Black
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(16.dp)
    ) {
        // Species name
        Text(
            text = species.name.replaceFirstChar { it.uppercase() },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        // Capture rate
        Text(
            text = "Capture Rate: ${species.capture_rate}",
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.8f)
        )

        // Pokémon list under species
        if (species.pokemon_v2_pokemons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pokémon:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            species.pokemon_v2_pokemons.forEach { pokemon ->
                Text(
                    text = "  • ${pokemon.name.replaceFirstChar { it.uppercase() }}",
                    fontSize = 14.sp,
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPokemonClick(pokemon.id) }
                        .padding(vertical = 2.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No Pokémon variants",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}
