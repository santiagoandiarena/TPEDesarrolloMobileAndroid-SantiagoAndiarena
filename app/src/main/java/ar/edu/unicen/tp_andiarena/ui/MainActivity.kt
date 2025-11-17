package ar.edu.unicen.tp_andiarena.ui
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.edu.unicen.tp_andiarena.databinding.ActivityMainBinding
import ar.edu.unicen.tp_andiarena.ui.adapters.GamesAdapter
import ar.edu.unicen.tp_andiarena.ui.viewmodels.GamesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: GamesViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var gamesAdapter: GamesAdapter
    private lateinit var searchView: SearchView
    private val filtersLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            //obtiene los filtros
            val filters = result.data?.getParcelableExtra<ar.edu.unicen.tp_andiarena.data.model.GameFilters>("filters")
            filters?.let {
                //aplica filtros en el ViewModel
                viewModel.applyFilters(it)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        searchView = binding.searchView
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSearchView()
    }
    private fun setupRecyclerView() {
        gamesAdapter = GamesAdapter(
            onItemClick = { game ->
                // Navegar a la pantalla de detalles
                val intent = Intent(this, GameDetailActivity::class.java).apply {
                    putExtra("game_id", game.id)
                }
                startActivity(intent)
            },
            onLoadMore = {
                // Cuando el Adapter llama a onLoadMore, este llama a loadMoreGames
                viewModel.loadMoreGames()
            }
        )
        binding.recyclerViewGames.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = gamesAdapter
            setPadding(0, 0, 0, 56)
        }
        // ocultar teclado al scrollear
        binding.recyclerViewGames.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })
    }
    private fun setupObservers() {
        viewModel.loading.onEach { loading ->
            if (loading && viewModel.games.value.isEmpty()) {
                // Solo mostrar loading en la carga inicial
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerViewGames.visibility = View.GONE
                binding.textError.visibility = View.GONE
                binding.buttonRetry.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.recyclerViewGames.visibility = View.VISIBLE
            }
        }.launchIn(lifecycleScope)

        viewModel.games.onEach { games ->
            gamesAdapter.submitList(games)
            if (games.isEmpty() && !viewModel.loading.value) {
                val searchQuery = viewModel.currentSearch.value
                val message = if (!searchQuery.isNullOrBlank()) {
                    "No se encuentra el videojuego buscado"
                } else {
                    "No se encontraron juegos"
                }
                showMessage(message)
            }
        }.launchIn(lifecycleScope)
        // Observar errores
        viewModel.error.onEach { error ->
            error?.let {
                binding.textError.text = it
                binding.textError.visibility = View.VISIBLE
                binding.buttonRetry.visibility = View.VISIBLE
                binding.recyclerViewGames.visibility = View.GONE
            }
        }.launchIn(lifecycleScope)
    }
    private fun setupClickListeners() {
        binding.buttonRetry.setOnClickListener {
            viewModel.retry()
        }
        binding.buttonTempFilter.setOnClickListener {
            openFiltersScreen()
        }
    }
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            // se activa cada vez que cambia el texto de la busqueda
            override fun onQueryTextChange(newText: String?): Boolean {
                // Llama al ViewModel con el nuevo texto de búsqueda
                viewModel.searchGames(newText)
                return true
            }
        })
        searchView.setOnClickListener {
            searchView.isIconified = false
            searchView.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
        }
    }
    private fun openFiltersScreen() {
        val intent = Intent(this, FiltersActivity::class.java)
        filtersLauncher.launch(intent)
    }
    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}