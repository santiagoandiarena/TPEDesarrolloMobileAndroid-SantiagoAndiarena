package ar.edu.unicen.tp_andiarena.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ar.edu.unicen.tp_andiarena.databinding.ActivityGameDetailBinding
import ar.edu.unicen.tp_andiarena.ui.viewmodels.GameDetailViewModel
import coil.load
import coil.request.CachePolicy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class GameDetailActivity : AppCompatActivity() {

    private val viewModel: GameDetailViewModel by viewModels()
    private lateinit var binding: ActivityGameDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val gameId = intent.getIntExtra("game_id", -1)
        if (gameId == -1) {
            finish()
            return
        }

        setupObservers()
        setupClickListeners()
        viewModel.loadGameDetails(gameId)
    }

    private fun setupObservers() {
        viewModel.game.onEach { game ->
            game?.let {
                bindGameData(it)
                setupActionButtons(it) // Configurar botones cuando el juego esté cargado
            }
        }.launchIn(lifecycleScope)

        viewModel.loading.onEach { loading ->
            if (loading) {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.scrollView.visibility = android.view.View.GONE
                binding.textError.visibility = android.view.View.GONE
                binding.buttonRetry.visibility = android.view.View.GONE
            } else {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }.launchIn(lifecycleScope)

        viewModel.error.onEach { error ->
            error?.let {
                binding.textError.text = it
                binding.textError.visibility = android.view.View.VISIBLE
                binding.buttonRetry.visibility = android.view.View.VISIBLE
                binding.scrollView.visibility = android.view.View.GONE
                binding.progressBar.visibility = android.view.View.GONE
            } ?: run {
                binding.textError.visibility = android.view.View.GONE
                binding.buttonRetry.visibility = android.view.View.GONE
            }
        }.launchIn(lifecycleScope)
    }

    private fun bindGameData(game: ar.edu.unicen.tp_andiarena.data.model.Game) {
        binding.apply {
            textGameName.text = game.name
            textGameRating.text = "Rating: ${game.rating}"
            textReleaseDate.text = "Lanzamiento: ${game.released ?: "No disponible"}"

            //descripcion
            textDescription.text = game.description ?: "Sin descripción disponible"

            // Generos
            val genresText = game.genres?.joinToString { it.name } ?: "No disponible"
            textGenres.text = "Géneros: $genresText"

            // Plataformas
            val platformsText = game.platforms?.take(5)?.joinToString { it.platform.name } ?: "No disponible"
            textPlatforms.text = "Plataformas: $platformsText"

            // Tiendas
            val storesText = game.stores?.take(3)?.joinToString { it.store.name } ?: "No disponible"
            textStores.text = "Disponible en: $storesText"

            // cargar img
            imageGame.load(game.backgroundImage) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }

            scrollView.visibility = android.view.View.VISIBLE
            textError.visibility = android.view.View.GONE
            buttonRetry.visibility = android.view.View.GONE
        }
    }

    private fun setupActionButtons(game: ar.edu.unicen.tp_andiarena.data.model.Game) {
        // Botón de compartir
        binding.buttonShare.setOnClickListener {
            shareGame(game)
        }

        // Botón de sitio web
        binding.buttonWebsite.setOnClickListener {
            openGameWebsite(game)
        }

        // Mostrar u ocultar botón de sitio web según disponibilidad
        if (game.website.isNullOrBlank()) {
            binding.buttonWebsite.visibility = android.view.View.GONE
        } else {
            binding.buttonWebsite.visibility = android.view.View.VISIBLE
        }
    }

    private fun shareGame(game: ar.edu.unicen.tp_andiarena.data.model.Game) {
        val shareText = buildString {
            append("${game.name}\n\n")
            append("Rating: ${game.rating}\n")
            game.released?.let { append("Lanzamiento: $it\n") }
            game.genres?.let { genres ->
                append("Géneros: ${genres.joinToString { it.name }}\n")
            }
            append("\n")
            append("Sinopsis:\n")
            append(game.description ?: "Sin descripción disponible")
            append("\n\n")
            append("Descubre más videojuegos en GamesHub!")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Mira este videojuego: ${game.name}")
            type = "text/plain"
        }

        //chooser para q el usuario seleccione la app
        val chooserIntent = Intent.createChooser(shareIntent, "Compartir juego")

        // Verificar si hay apps que puedan manejar esta acción
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(chooserIntent)
        } else {
            // Mostrar mensaje si no hay apps disponibles
            android.widget.Toast.makeText(
                this,
                "No hay aplicaciones disponibles para compartir",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openGameWebsite(game: ar.edu.unicen.tp_andiarena.data.model.Game) {
        game.website?.let { websiteUrl ->
            try {
                // Asegurarse de que la URL tenga el protocolo
                val url = if (websiteUrl.startsWith("http")) {
                    websiteUrl
                } else {
                    "https://$websiteUrl"
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

                // Verificar si hay un navegador disponible
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    android.widget.Toast.makeText(
                        this,
                        "No hay un navegador disponible",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    this,
                    "Error al abrir el sitio web",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } ?: run {
            android.widget.Toast.makeText(
                this,
                "Este juego no tiene sitio web disponible",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupClickListeners() {
        // Botón de volver
        binding.buttonBack.setOnClickListener {
            finish()
        }

        // Botón de reintentar
        binding.buttonRetry.setOnClickListener {
            val gameId = intent.getIntExtra("game_id", -1)
            if (gameId != -1) {
                viewModel.loadGameDetails(gameId)
            }
        }
    }
}