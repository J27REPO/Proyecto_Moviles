package es.uniovi.recetasasturianas.ui.detail

import android.os.Bundle
import androidx.navigation.NavDirections
import es.uniovi.recetasasturianas.R
import kotlin.Int
import kotlin.String

public class RecipeDetailFragmentDirections private constructor() {
  private data class ActionDetailToWebView(
    public val url: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_detail_to_webView

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("url", this.url)
        return result
      }
  }

  public companion object {
    public fun actionDetailToWebView(url: String): NavDirections = ActionDetailToWebView(url)
  }
}
