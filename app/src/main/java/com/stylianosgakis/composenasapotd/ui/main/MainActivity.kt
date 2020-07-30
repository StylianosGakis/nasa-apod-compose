package com.stylianosgakis.composenasapotd.ui.main

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.collectAsState
import androidx.compose.launchInComposition
import androidx.compose.onActive
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.drawOpacity
import androidx.ui.core.layoutId
import androidx.ui.core.setContent
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.asImageAsset
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.ConstraintLayout
import androidx.ui.layout.ConstraintSet2
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.material.Button
import androidx.ui.material.Card
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.unit.dp
import coil.ImageLoader
import coil.request.GetRequestBuilder
import com.stylianosgakis.composenasapotd.composeconfig.ComposeNasaPOTDTheme
import com.stylianosgakis.composenasapotd.model.NasaDate
import com.stylianosgakis.composenasapotd.model.NasaPhoto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.time.LocalDate

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), KoinComponent {

    private val viewModel by viewModel<MainViewModel>()
    private val imageLoader: ImageLoader by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeNasaPOTDTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(imageLoader, viewModel.mainViewState, viewModel::startEvent)
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@Composable
fun MainScreen(
    imageLoader: ImageLoader,
    stateFlow: StateFlow<MainViewState>,
    sendEvent: (MainStateEvent) -> Unit,
) {
    val viewState = stateFlow.collectAsState()
    val lastDate = state { LocalDate.now() }

    val daysToGet = 10L

    fun get10MoreDays() {
        sendEvent(
            MainStateEvent.DownloadPhotosBetweenDates(
                startDate = NasaDate.fromLocalDate(lastDate.value.minusDays(daysToGet)),
                endDate = NasaDate.fromLocalDate(lastDate.value)
            )
        )
        lastDate.value = lastDate.value.minusDays(daysToGet)
    }

    onActive { get10MoreDays() }

    Column(
        horizontalGravity = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (viewState.value.isLoading()) {
            CircularProgressIndicator()
            Text(text = "Loading")
        } else {
            Button(
                onClick = { get10MoreDays() }
            ) {
                Text("Load 10 more days")
            }
        }
        LazyColumnItems(items = viewState.value.listOfPhotos) { nasaPhoto ->
            NasaPhotoCard(imageLoader, nasaPhoto = nasaPhoto)
        }
    }
}

@Composable
private val modifier = Modifier.layoutId("Image")

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@Composable
fun NasaPhotoCard(imageLoader: ImageLoader, nasaPhoto: NasaPhoto) {
    val context = ContextAmbient.current
    val (bitmap, setBitmap) = state<Bitmap?> { null }

    launchInComposition(nasaPhoto.url) {
        if (nasaPhoto.isImage().not()) return@launchInComposition
        val urlImage = imageLoader.execute(
            GetRequestBuilder(context)
                .data(nasaPhoto.url)
                .build()
        ).drawable
        setBitmap((urlImage as? BitmapDrawable)?.bitmap)
    }

    Card(
        shape = RoundedCornerShape(4.dp),
        elevation = 4.dp,
        modifier = Modifier.padding(16.dp),
    ) {
        ConstraintLayout(
            constraintSet = ConstraintSet2 {
                val image = createRefFor("Image")
                val text = createRefFor("Text")
                constrain(image) {
                    start to parent.start
                    end to parent.end
                    top to parent.top
                    bottom to parent.bottom
                }
                constrain(text) {
                    start to image.start
                    bottom to image.bottom
                }
            }
        ) {
            val imageAsset = bitmap?.asImageAsset()
            if (imageAsset != null) {
                Image(
                    modifier = Modifier.fillMaxWidth() + Modifier.layoutId("Image"),
                    contentScale = ContentScale.FillWidth,
                    asset = imageAsset,
                )
            } else {
                Text(text = "Media type not supported")
            }
            Surface(
                modifier = Modifier.drawOpacity(0.8f),
                color = Color.Black,
            ) {
                Text(color = Color.White, text = nasaPhoto.title)
            }
        }
    }
}

