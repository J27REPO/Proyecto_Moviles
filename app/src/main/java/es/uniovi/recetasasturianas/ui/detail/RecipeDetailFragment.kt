package es.uniovi.recetasasturianas.ui.detail

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import es.uniovi.recetasasturianas.MainActivity
import es.uniovi.recetasasturianas.R
import es.uniovi.recetasasturianas.databinding.FragmentRecipeDetailBinding

/**
 * Fragment para el detalle de una receta (pantalla detalle).
 */
class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeDetailViewModel by viewModels()
    
    private var isTwoPane: Boolean = false
    private var currentRecipe: es.uniovi.recetasasturianas.data.model.Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isTwoPane = activity?.findViewById<View>(R.id.detail_container) != null
        
        val recipeId = arguments?.getInt("recipeId") ?: 0
        viewModel.setRecipeId(recipeId)
        
        observeViewModel()
        setupFab()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareRecipe()
                true
            }
            R.id.action_share_text -> {
                shareRecipeAsText()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeViewModel() {
        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            recipe?.let { 
                currentRecipe = it
                bindRecipe(it) 
            }
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            updateFabIcon(isFavorite)
        }

        viewModel.favoriteMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                val text = if (it == "added") R.string.favorite_added else R.string.favorite_removed
                Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
                viewModel.clearFavoriteMessage()
            }
        }
    }

    private fun bindRecipe(recipe: es.uniovi.recetasasturianas.data.model.Recipe) {
        binding.apply {
            // Imagen
            recipe.imageUrl?.let { url ->
                Glide.with(imageRecipe.context)
                    .load(url)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageRecipe)
            } ?: imageRecipe.setImageResource(R.drawable.placeholder_recipe)

            // Nombre
            textName.text = recipe.name

            // Restaurante
            textRestaurant.text = getString(R.string.by_restaurant, recipe.restaurant)

            // Tiempo
            if (recipe.timeMinutes != null) {
                textTime.text = getString(R.string.time_format, recipe.timeMinutes!!)
                textTime.isVisible = true
            } else {
                textTime.isVisible = false
            }

            // Ingredientes (HTML)
            if (recipe.ingredientsHtml.isNotBlank()) {
                textIngredients.text = HtmlCompat.fromHtml(
                    recipe.ingredientsHtml,
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                textIngredients.movementMethod = LinkMovementMethod.getInstance()
                groupIngredients.isVisible = true
            } else {
                groupIngredients.isVisible = false
            }

            // Preparación (HTML)
            if (recipe.preparationHtml.isNotBlank()) {
                textPreparation.text = HtmlCompat.fromHtml(
                    recipe.preparationHtml,
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                textPreparation.movementMethod = LinkMovementMethod.getInstance()
                groupPreparation.isVisible = true
            } else {
                groupPreparation.isVisible = false
            }

            // Trucos y consejos (HTML)
            if (!recipe.tipsHtml.isNullOrBlank()) {
                textTips.text = HtmlCompat.fromHtml(
                    recipe.tipsHtml!!,
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                textTips.movementMethod = LinkMovementMethod.getInstance()
                groupTips.isVisible = true
            } else {
                groupTips.isVisible = false
            }

            // Notas (HTML)
            if (!recipe.notesHtml.isNullOrBlank()) {
                textNotes.text = HtmlCompat.fromHtml(
                    recipe.notesHtml!!,
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                textNotes.movementMethod = LinkMovementMethod.getInstance()
                groupNotes.isVisible = true
            } else {
                groupNotes.isVisible = false
            }

            // Botón ver restaurante
            if (recipe.restaurantUrl != null) {
                buttonRestaurant.setOnClickListener {
                    if (isTwoPane) {
                        showWebViewInTablet(recipe.restaurantUrl!!)
                    } else {
                        navigateToWebView(recipe.restaurantUrl!!)
                    }
                }
                buttonRestaurant.isVisible = true
            } else {
                buttonRestaurant.isVisible = false
            }
        }
    }

    private fun setupFab() {
        binding.fabFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }
    }

    private fun updateFabIcon(isFavorite: Boolean) {
        binding.fabFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_outline
        )
    }

    private fun navigateToWebView(url: String) {
        val bundle = Bundle().apply { putString("url", url) }
        findNavController().navigate(R.id.action_detail_to_webView, bundle)
    }

    private fun showWebViewInTablet(url: String) {
        (activity as? MainActivity)?.let { main ->
            val webViewFragment = es.uniovi.recetasasturianas.ui.webview.RestaurantWebViewFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
                }
            }
            main.supportFragmentManager.beginTransaction()
                .replace(R.id.detail_container, webViewFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun shareRecipeAsText() {
        val recipe = currentRecipe ?: return
        
        val shareText = buildString {
            append("🍽️ ${recipe.name}\n")
            append("📍 ${getString(R.string.by_restaurant, recipe.restaurant)}\n")
            recipe.timeMinutes?.let {
                append("⏱️ ${getString(R.string.time_format, it)}\n")
            }
            append("\n")
            append("📝 ${getString(R.string.ingredients)}:\n")
            append(stripHtml(recipe.ingredientsHtml))
            append("\n\n")
            append("👨‍🍳 ${getString(R.string.preparation)}:\n")
            append(stripHtml(recipe.preparationHtml))
            recipe.restaurantUrl?.let {
                append("\n\n🔗 ${getString(R.string.view_restaurant)}: $it")
            }
            append("\n\n📱 ${getString(R.string.app_name)}")
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, recipe.name)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_recipe)))
    }

    private fun shareRecipe() {
        val recipe = currentRecipe ?: return
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.share_recipe)
            .setItems(R.array.share_options) { _, which ->
                when (which) {
                    0 -> shareRecipeAsText()
                    1 -> shareRecipeLink(recipe)
                }
            }
            .show()
    }

    private fun shareRecipeLink(recipe: es.uniovi.recetasasturianas.data.model.Recipe) {
        val shareText = buildString {
            append("🍽️ ${recipe.name}\n")
            append("📍 ${getString(R.string.by_restaurant, recipe.restaurant)}\n")
            recipe.restaurantUrl?.let {
                append("\n🔗 $it")
            }
            append("\n\n📱 ${getString(R.string.app_name)}")
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, recipe.name)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_recipe)))
    }

    private fun stripHtml(html: String): String {
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
