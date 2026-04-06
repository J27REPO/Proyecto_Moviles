package es.uniovi.recetasasturianas.ui.list

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import es.uniovi.recetasasturianas.MainActivity
import es.uniovi.recetasasturianas.R
import es.uniovi.recetasasturianas.databinding.FragmentRecipeListBinding

/**
 * Fragment para la lista de recetas (pantalla principal / maestro).
 *
 * Implementa:
 * - RecyclerView con lista de recetas
 * - Búsqueda por nombre/restaurante/ingredientes (capa de datos)
 * - Filtrado por tiempo (capa UI)
 * - Pull-to-refresh
 * - Soporte para maestro-detalle en tablets
 * - Indicador de modo offline
 */
class RecipeListFragment : Fragment() {

    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeListViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter
    
    // Indica si estamos en modo tablet (dos paneles)
    private var isTwoPane: Boolean = false
    // ID de la receta seleccionada actualmente (para tablets)
    private var selectedRecipeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Detectar modo tablet
        isTwoPane = activity?.findViewById<View>(R.id.detail_container) != null

        setupRecyclerView()
        setupSwipeRefresh()
        setupFilterChips()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter { recipe ->
            if (isTwoPane) {
                // En tablet, mostrar en panel derecho
                selectedRecipeId = recipe.id
                adapter.setSelectedRecipe(recipe.id)
                (activity as? MainActivity)?.let { main ->
                    val detailFragment = es.uniovi.recetasasturianas.ui.detail.RecipeDetailFragment().apply {
                        arguments = Bundle().apply {
                            putInt("recipeId", recipe.id)
                        }
                    }
                    // Ocultar placeholder
                    activity?.findViewById<View>(R.id.text_placeholder)?.visibility = View.GONE
                    main.supportFragmentManager.beginTransaction()
                        .replace(R.id.detail_container, detailFragment)
                        .commit()
                }
            } else {
                // En teléfono, navegar
                navigateToDetail(recipe.id)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RecipeListFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when {
                checkedIds.contains(R.id.chip_quick) -> TimeFilter.QUICK
                checkedIds.contains(R.id.chip_medium) -> TimeFilter.MEDIUM
                checkedIds.contains(R.id.chip_long) -> TimeFilter.LONG
                else -> TimeFilter.ALL
            }
            viewModel.applyTimeFilter(filter)
        }
    }

    private fun observeViewModel() {
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
            binding.emptyView.isVisible = recipes.isEmpty()
        }

        viewModel.timeFilter.observe(viewLifecycleOwner) { filter ->
            val chipId = when (filter) {
                TimeFilter.ALL -> View.NO_ID // Ninguno marcado si es ALL
                TimeFilter.QUICK -> R.id.chip_quick
                TimeFilter.MEDIUM -> R.id.chip_medium
                TimeFilter.LONG -> R.id.chip_long
            }
            if (chipId != View.NO_ID) {
                binding.chipGroupFilter.check(chipId)
            } else {
                binding.chipGroupFilter.clearCheck()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.progressBar.isVisible = isLoading && adapter.currentList.isEmpty()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { viewModel.loadRecipes() }
                    .show()
                viewModel.clearError()
            }
        }

        // Mostrar/ocultar banner de offline
        viewModel.isOffline.observe(viewLifecycleOwner) { isOffline ->
            binding.offlineBanner.isVisible = isOffline
        }
    }

    private fun navigateToDetail(recipeId: Int) {
        val bundle = Bundle().apply { putInt("recipeId", recipeId) }
        findNavController().navigate(R.id.action_list_to_detail, bundle)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            queryHint = getString(R.string.search_hint)
            imeOptions = EditorInfo.IME_ACTION_SEARCH

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.search(query.orEmpty())
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.search(newText.orEmpty())
                    return true
                }
            })
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        // Refrescar preferencias cuando se vuelve a este fragment
        viewModel.refreshPreferences()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
