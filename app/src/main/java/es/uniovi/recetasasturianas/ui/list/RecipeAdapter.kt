package es.uniovi.recetasasturianas.ui.list

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import es.uniovi.recetasasturianas.R
import es.uniovi.recetasasturianas.data.model.Recipe
import es.uniovi.recetasasturianas.databinding.ItemRecipeBinding

/**
 * Adapter para el RecyclerView de recetas.
 *
 * Soporta selección para modo tablet.
 */
class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    // ID de receta seleccionada (para tablets)
    private var selectedRecipeId: Int? = null

    /**
     * Establece el ID de la receta seleccionada (para tablets).
     */
    fun setSelectedRecipe(recipeId: Int?) {
        val previousId = selectedRecipeId
        selectedRecipeId = recipeId
        
        // Notificar cambios solo en los items afectados
        previousId?.let { id ->
            val index = currentList.indexOfFirst { it.id == id }
            if (index >= 0) notifyItemChanged(index)
        }
        recipeId?.let { id ->
            val index = currentList.indexOfFirst { it.id == id }
            if (index >= 0) notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding, onRecipeClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        holder.bind(recipe, recipe.id == selectedRecipeId)
    }

    class RecipeViewHolder(
        private val binding: ItemRecipeBinding,
        private val onRecipeClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe, isSelected: Boolean) {
            binding.apply {
                // Nombre de la receta
                textRecipeName.text = recipe.name

                // Restaurante
                textRestaurant.text = root.context.getString(R.string.by_restaurant, recipe.restaurant)

                // Tiempo de preparación
                recipe.timeMinutes?.let { minutes ->
                    textTime.text = root.context.getString(R.string.time_format, minutes)
                    textTime.visibility = View.VISIBLE
                } ?: run {
                    textTime.visibility = View.GONE
                }

                // Imagen
                recipe.imageUrl?.let { url ->
                    Glide.with(imageRecipe.context)
                        .load(url)
                        .placeholder(R.drawable.placeholder_recipe)
                        .error(R.drawable.placeholder_recipe)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("GlideError", "Fallo al cargar imagen: $url", e)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }
                        })
                        .into(imageRecipe)
                } ?: run {
                    imageRecipe.setImageResource(R.drawable.placeholder_recipe)
                }

                // Estado seleccionado (para tablets)
                if (isSelected) {
                    root.setBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.selected_background)
                    )
                } else {
                    root.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.transparent)
                    )
                }

                // Click listener
                root.setOnClickListener { onRecipeClick(recipe) }
            }
        }
    }

    /**
     * DiffUtil para actualizaciones eficientes del RecyclerView.
     */
    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Enum para los filtros de tiempo.
 */
enum class TimeFilter {
    ALL,    // Sin filtro
    QUICK,  // ≤ 30 minutos
    MEDIUM, // 31-60 minutos
    LONG    // > 60 minutos
}
