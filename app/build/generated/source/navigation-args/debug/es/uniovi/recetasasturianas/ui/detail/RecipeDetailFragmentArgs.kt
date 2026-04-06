package es.uniovi.recetasasturianas.ui.detail

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.jvm.JvmStatic

public data class RecipeDetailFragmentArgs(
  public val recipeId: Int,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putInt("recipeId", this.recipeId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("recipeId", this.recipeId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): RecipeDetailFragmentArgs {
      bundle.setClassLoader(RecipeDetailFragmentArgs::class.java.classLoader)
      val __recipeId : Int
      if (bundle.containsKey("recipeId")) {
        __recipeId = bundle.getInt("recipeId")
      } else {
        throw IllegalArgumentException("Required argument \"recipeId\" is missing and does not have an android:defaultValue")
      }
      return RecipeDetailFragmentArgs(__recipeId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): RecipeDetailFragmentArgs {
      val __recipeId : Int?
      if (savedStateHandle.contains("recipeId")) {
        __recipeId = savedStateHandle["recipeId"]
        if (__recipeId == null) {
          throw IllegalArgumentException("Argument \"recipeId\" of type integer does not support null values")
        }
      } else {
        throw IllegalArgumentException("Required argument \"recipeId\" is missing and does not have an android:defaultValue")
      }
      return RecipeDetailFragmentArgs(__recipeId)
    }
  }
}
