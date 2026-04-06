package es.uniovi.recetasasturianas.ui.webview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import es.uniovi.recetasasturianas.databinding.FragmentWebViewBinding

/**
 * Fragment que muestra el sitio web del restaurante en un WebView embebido.
 *
 * IMPORTANTE: Este es un requisito del profesor.
 * El enlace al restaurante debe abrirse DENTRO de la app, no en el navegador externo.
 */
class RestaurantWebViewFragment : Fragment() {

    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!

    private var url: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener URL de los argumentos
        url = arguments?.getString("url")
        
        setupToolbar()
        setupWebView()
        loadUrl()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                // Habilitar JavaScript para sitios que lo necesitan
                javaScriptEnabled = true
                // Soporte para zoom
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                // Caché
                domStorageEnabled = true
                // Cargar imágenes automáticamente
                loadsImagesAutomatically = true
            }

            // WebViewClient personalizado para:
            // 1. Evitar que se abra el navegador externo
            // 2. Manejar errores de carga
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    // No sobrescribir, cargar dentro del WebView
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    binding.progressBar.isVisible = true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.progressBar.isVisible = false
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        binding.webView.isVisible = false
                        binding.errorView.isVisible = true
                    }
                }
            }
        }
    }

    private fun loadUrl() {
        val url = url
        if (!url.isNullOrEmpty()) {
            binding.webView.loadUrl(url)
        } else {
            binding.webView.isVisible = false
            binding.errorView.isVisible = true
        }
    }

    override fun onDestroyView() {
        binding.webView.apply {
            stopLoading()
            settings.javaScriptEnabled = false
        }
        super.onDestroyView()
        _binding = null
    }
}
