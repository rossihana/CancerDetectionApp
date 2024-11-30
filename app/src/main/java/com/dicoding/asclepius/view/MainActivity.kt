package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.adapter.NewsAdapter
import com.dicoding.asclepius.api.ApiConfig
import com.dicoding.asclepius.api.ArticlesItem
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var results: String? = null

    private val viewModel: MainViewModel by viewModels()

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.currentImageUri = uri
            showImage()
        } else {
            showToast("Gagal mengambil gambar")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showImage()
        fetchCancerNews()


        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showImage() {
        viewModel.currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage() {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        showToast(error)
                    }
                }

                override fun onResults(result: List<Classifications>?, inferenceTime: Long) {
                    runOnUiThread {
                        result?.let {
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                val confidence = it[0].categories[0].score * 100
                                val confidencePercentage = String.format("%.0f%%", confidence)
                                val label = it[0].categories[0].label
                                results = "$label $confidencePercentage"
                                moveToResult()
                            }
                        }
                    }
                }
            }
        )

        imageClassifierHelper.classifyStaticImage(viewModel.currentImageUri?: Uri.EMPTY)
    }

    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_RESULT, results)
        intent.putExtra(ResultActivity.EXTRA_IMAGE, viewModel.currentImageUri.toString())
        startActivity(intent)
    }

    private fun fetchCancerNews() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = ApiConfig.getApiService().getCancerNews(
                    query = "cancer",
                    category = "health",
                    language = "en",
                    apiKey = "c8138415582f4c6b9b8c02a775ed47c7"
                )

                if (response.isSuccessful) {
                    val articles = response.body()?.articles?.filterNotNull() ?: emptyList()
                    showNews(articles)
                } else {
                    showToast("Gagal memuat berita")
                }
            } catch (e: Exception) {
                showToast("Gagal terhubung ke server: ${e.localizedMessage}")
            }
        }
    }



    private fun showNews(articles: List<ArticlesItem>) {
        val adapter = NewsAdapter(articles)
        binding.rvNews.layoutManager = LinearLayoutManager(this)
        binding.rvNews.adapter = adapter
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}