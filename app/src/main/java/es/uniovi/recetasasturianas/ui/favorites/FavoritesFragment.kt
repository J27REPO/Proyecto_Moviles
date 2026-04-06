package es.uniovi.recetasasturianas.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import es.uniovi.recetasasturianas.MainActivity
import es.uniovi.recetasasturianas.R
import es.uniovi.recetasasturianas.databinding.FragmentFavoritesBinding
import es.uniovi.recetasasturianas.ui.list.RecipeAdapter

/**
 * Fragment para la lista de recetas favoritas.
 *
 * Muestra las recetas marcadas como favoritas por el usuario.
 * Soporta modo tablet (maestro-detalle).
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter
    
    // Indica si estamos en modo tablet
    private var isTwoPane: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Detectar modo tablet
        isTwoPane = activity?.findViewById<View>(R.id.detail_container) != null

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter { recipe ->
            if (isTwoPane) {
                // En tablet, mostrar en panel derecho
                (activity as? MainActivity)?.let { main ->
                    val detailFragment = es.uniovi.recetasasturianas.ui.detail.RecipeDetailFragment().apply {
                        arguments = Bundle().apply {
                            putInt("recipeId", recipe.id)
                        }
                    }
                    activity?.findViewById<View>(R.id.text_placeholder)?.visibility = View.GONE
                    main.supportFragmentManager.beginTransaction()
                        .replace(R.id.detail_container, detailFragment)
                        .commit()
                }
            } else {
                navigateToDetail(recipe.id)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoritesFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            adapter.submitList(favorites)
            binding.emptyView.isVisible = favorites.isEmpty()
            binding.recyclerView.isVisible = favorites.isNotEmpty()
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                if (it == "removed") {
                    Snackbar.make(binding.root, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show()
                }
                viewModel.clearMessage()
            }
        }
    }

    private fun navigateToDetail(recipeId: Int) {
        val bundle = Bundle().apply { putInt("recipeId", recipeId) }
        findNavController().navigate(R.id.action_favorites_to_detail, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
