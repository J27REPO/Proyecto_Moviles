package es.uniovi.recetasasturianas.ui.webview

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class RestaurantWebViewFragmentArgs(
  public val url: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("url", this.url)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("url", this.url)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): RestaurantWebViewFragmentArgs {
      bundle.setClassLoader(RestaurantWebViewFragmentArgs::class.java.classLoader)
      val __url : String?
      if (bundle.containsKey("url")) {
        __url = bundle.getString("url")
        if (__url == null) {
          throw IllegalArgumentException("Argument \"url\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"url\" is missing and does not have an android:defaultValue")
      }
      return RestaurantWebViewFragmentArgs(__url)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        RestaurantWebViewFragmentArgs {
      val __url : String?
      if (savedStateHandle.contains("url")) {
        __url = savedStateHandle["url"]
        if (__url == null) {
          throw IllegalArgumentException("Argument \"url\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"url\" is missing and does not have an android:defaultValue")
      }
      return RestaurantWebViewFragmentArgs(__url)
    }
  }
}
