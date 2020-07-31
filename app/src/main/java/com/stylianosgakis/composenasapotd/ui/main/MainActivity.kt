package com.stylianosgakis.composenasapotd.ui.main

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.collectAsState
import androidx.compose.launchInComposition
import androidx.compose.onActive
import androidx.compose.onPreCommit
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.layoutId
import androidx.ui.core.setContent
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentGravity
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.asImageAsset
import androidx.ui.layout.Column
import androidx.ui.layout.defaultMinSizeConstraints
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.material.Button
import androidx.ui.material.Card
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.text.style.TextAlign
import androidx.ui.unit.dp
import coil.ImageLoader
import coil.request.GetRequestBuilder
import com.stylianosgakis.composenasapotd.composeconfig.ComposeNasaPOTDTheme
import com.stylianosgakis.composenasapotd.model.NasaDate
import com.stylianosgakis.composenasapotd.model.NasaPhoto
import com.stylianosgakis.composenasapotd.model.toLocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
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
    val (lastDate, setLastDate) = state { LocalDate.now() }

    val daysToGet = 10L

    onActive {
        sendEvent(MainStateEvent.FetchDatabasePhotos)
    }

    onPreCommit(viewState.value.listOfPhotos) {
        viewState.value.listOfPhotos.lastOrNull()?.date?.toLocalDate()?.let(setLastDate)
    }

    fun get10MoreDays() {
        viewState.value.listOfPhotos.lastOrNull()?.date?.toLocalDate()?.let(setLastDate)
        sendEvent(
            MainStateEvent.DownloadPhotosBetweenDates(
                startDate = NasaDate.fromLocalDate(lastDate.minusDays(daysToGet)),
                endDate = NasaDate.fromLocalDate(lastDate)
            )
        )
        setLastDate(lastDate.minusDays(daysToGet))
    }

    val nasaPhotos = viewState.value.listOfPhotos

    if (nasaPhotos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            gravity = ContentGravity.Center
        ) {
            LoadMoreButton(loading = viewState.value.isLoading(), onClick = ::get10MoreDays)
        }
    } else {
        LazyColumnItems(items = nasaPhotos) { nasaPhoto ->
            if (nasaPhotos.last() == nasaPhoto) {
                Column(horizontalGravity = Alignment.CenterHorizontally) {
                    NasaPhotoCard(imageLoader, nasaPhoto = nasaPhoto)
                    LoadMoreButton(
                        modifier = Modifier.padding(20.dp),
                        loading = viewState.value.isLoading(),
                        onClick = ::get10MoreDays,
                    )
                }
            } else {
                NasaPhotoCard(imageLoader, nasaPhoto = nasaPhoto)
            }
        }
    }
}

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@Composable
fun NasaPhotoCard(imageLoader: ImageLoader, nasaPhoto: NasaPhoto) {
    val context = ContextAmbient.current
    val (sdBitmap, setSdBitmap) = state<Bitmap?> { null }
    val (hdBitmap, setHdBitmap) = state<Bitmap?> { null }
    val imageAsset = sdBitmap?.asImageAsset()
    val hdImageAsset = hdBitmap?.asImageAsset()

    launchInComposition(nasaPhoto.url) {
        val sdAsync = async {
            val sdRequest = GetRequestBuilder(context).data(nasaPhoto.url).build()
            return@async imageLoader.execute(sdRequest).drawable
        }
        val hdAsync = async {
            val hdRequest = GetRequestBuilder(context).data(nasaPhoto.hdUrl).build()
            return@async imageLoader.execute(hdRequest).drawable
        }
        setSdBitmap((sdAsync.await() as? BitmapDrawable)?.bitmap)
        setHdBitmap((hdAsync.await() as? BitmapDrawable)?.bitmap)
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        modifier = Modifier.padding(8.dp),
    ) {
        val asset = hdImageAsset ?: imageAsset
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = when (asset) {
                    hdImageAsset -> Color.Black
                    imageAsset -> Color.Gray
                    else -> Color.White
                },
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White,
                    text = nasaPhoto.title,
                    textAlign = TextAlign.Center,
                )
            }
            Box(modifier = Modifier.defaultMinSizeConstraints(200.dp, 200.dp)) {
                if (hdImageAsset != null || imageAsset != null) {
                    val asset = hdImageAsset ?: imageAsset!!
                    Image(
                        modifier = Modifier.fillMaxWidth() + Modifier.layoutId("Image"),
                        contentScale = ContentScale.FillWidth,
                        asset = asset,
                    )
                } else {
                    Text(text = "Error loading item")
                }
            }
        }
    }
}

@Composable
fun LoadMoreButton(
    modifier: Modifier = Modifier,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Box(modifier = modifier) {
        if (loading) {
            Column {
                CircularProgressIndicator()
                Text(text = "Loading")
            }
        } else {
            Button(
                onClick = onClick,
            ) {
                Text("Load 10 more days")
            }
        }
    }
}

