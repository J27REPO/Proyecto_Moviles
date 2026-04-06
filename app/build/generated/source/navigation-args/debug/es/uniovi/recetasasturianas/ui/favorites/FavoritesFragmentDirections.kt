package es.uniovi.recetasasturianas.ui.favorites

import android.os.Bundle
import androidx.navigation.NavDirections
import es.uniovi.recetasasturianas.R
import kotlin.Int

public class FavoritesFragmentDirections private constructor() {
  private data class ActionFavoritesToDetail(
    public val recipeId: Int,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_favorites_to_detail

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("recipeId", this.recipeId)
        return result
      }
  }

  public companion object {
    public fun actionFavoritesToDetail(recipeId: Int): NavDirections =
        ActionFavoritesToDetail(recipeId)
  }
}
