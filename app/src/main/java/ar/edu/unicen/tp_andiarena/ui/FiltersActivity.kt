package ar.edu.unicen.tp_andiarena.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ar.edu.unicen.tp_andiarena.databinding.ActivityFiltersBinding
import ar.edu.unicen.tp_andiarena.ui.adapters.GenresAdapter
import ar.edu.unicen.tp_andiarena.ui.adapters.PlatformsAdapter
import ar.edu.unicen.tp_andiarena.ui.viewmodels.FiltersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class FiltersActivity : AppCompatActivity() { //permite al usuario seleccionar plataformas, géneros y un orden, y luego devuelve esos filtros a MainActivity

    private val viewModel: FiltersViewModel by viewModels()
    private lateinit var binding: ActivityFiltersBinding
    private lateinit var platformsAdapter: PlatformsAdapter
    private lateinit var genresAdapter: GenresAdapter

    // Opciones de ordenamiento
    private val orderingOptions = listOf(
        "Sin orden" to null,
        "Nombre (A-Z)" to "name",
        "Nombre (Z-A)" to "-name",
        "Rating (alto a bajo)" to "-rating",
        "Rating (bajo a alto)" to "rating",
        "Fecha de lanzamiento (nuevos primero)" to "-released",
        "Fecha de lanzamiento (viejos primero)" to "released"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFiltersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupRecyclerViews()
        setupOrderingSpinner()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        platformsAdapter = PlatformsAdapter { platform, isSelected ->
            val currentSelection = platformsAdapter.getSelectedItems()
            viewModel.updatePlatforms(currentSelection)
        }

        genresAdapter = GenresAdapter { genre, isSelected ->
            val currentSelection = genresAdapter.getSelectedItems()
            viewModel.updateGenres(currentSelection)
        }

        binding.recyclerPlatforms.apply {
            layoutManager = LinearLayoutManager(this@FiltersActivity)
            adapter = platformsAdapter
        }

        binding.recyclerGenres.apply {
            layoutManager = LinearLayoutManager(this@FiltersActivity)
            adapter = genresAdapter
        }
    }


    private fun setupOrderingSpinner() {
        val adapter = ArrayAdapter( //configura opciones del adaptador del spinner
            this,
            android.R.layout.simple_spinner_item,
            orderingOptions.map { it.first }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOrdering.adapter = adapter

        // Listener para cambios en el spinner
        binding.spinnerOrdering.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedOrdering = orderingOptions[position].second
                    viewModel.updateOrdering(selectedOrdering)
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
    }

    private fun setupObservers() {
        // Observar estado de loading
        viewModel.loading.onEach { loading ->
            if (loading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.textError.visibility = View.GONE
                binding.buttonRetry.visibility = View.GONE
                // Ocultar contenido mientras carga
                binding.spinnerOrdering.visibility = View.GONE
                binding.recyclerPlatforms.visibility = View.GONE
                binding.recyclerGenres.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.spinnerOrdering.visibility = View.VISIBLE
                binding.recyclerPlatforms.visibility = View.VISIBLE
                binding.recyclerGenres.visibility = View.VISIBLE
            }
        }.launchIn(lifecycleScope)

        // Observar plataformas
        viewModel.platforms.onEach { platforms ->
            platformsAdapter.submitList(platforms)
        }.launchIn(lifecycleScope)

        // Observar géneros
        viewModel.genres.onEach { genres ->
            genresAdapter.submitList(genres)
        }.launchIn(lifecycleScope)

        // Observar errores
        viewModel.error.onEach { error ->
            error?.let {
                binding.textError.text = it
                binding.textError.visibility = View.VISIBLE
                binding.buttonRetry.visibility = View.VISIBLE
            }
        }.launchIn(lifecycleScope)
    }

    private fun setupClickListeners() {
        binding.buttonRetry.setOnClickListener {
            viewModel.retry()
        }

        binding.buttonApply.setOnClickListener { //al pulsar aplicar devuelve los filtros seleccionados a MainActivity
            val filters = viewModel.getCurrentFilters()
            val resultIntent = Intent().apply {
                putExtra("filters", filters)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.buttonClear.setOnClickListener {
            // Limpiar selecciones
            platformsAdapter.clearSelection()
            genresAdapter.clearSelection()
            binding.spinnerOrdering.setSelection(0)
            viewModel.updateOrdering(null)
            viewModel.updatePlatforms(emptyList())
            viewModel.updateGenres(emptyList())
        }
    }
}