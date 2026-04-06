package es.uniovi.recetasasturianas.ui.list

import android.os.Bundle
import androidx.navigation.NavDirections
import es.uniovi.recetasasturianas.R
import kotlin.Int

public class RecipeListFragmentDirections private constructor() {
  private data class ActionListToDetail(
    public val recipeId: Int,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_list_to_detail

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("recipeId", this.recipeId)
        return result
      }
  }

  public companion object {
    public fun actionListToDetail(recipeId: Int): NavDirections = ActionListToDetail(recipeId)
  }
}
