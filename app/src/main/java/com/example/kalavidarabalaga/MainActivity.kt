package com.example.kalavidarabalaga


import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.kalavidarabalaga.ui.theme.KalavidaraBalagaTheme
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.grid.*
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KalavidaraBalagaTheme {
                ArtistListScreen()
            }
        }
    }
}

data class Troupe(
    val name: String,
    val artType: String,
    val district: String,
    val image: String,
    val phone: String,
    val equipment: String,
    val serviceArea: String,
    val description: String,
    val gallery: List<String>
)
@Composable
fun ArtistListScreen() {

    var selectedTroupe by remember { mutableStateOf<Troupe?>(null) }
    var troupeList by remember { mutableStateOf(listOf<Troupe>()) }
    var showAddScreen by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("artists")

            .addSnapshotListener { result,error ->

                if (error != null) {
                    Log.e("FIREBASE", "Error", error)
                    return@addSnapshotListener
                }

                val tempList = mutableListOf<Troupe>()

                result?.forEach { doc ->
                    val galleryList = (doc.get("gallery") as? List<*>)?.map { it.toString() } ?: emptyList()
                    val troupe = Troupe(
                        name = doc.getString("name") ?: "",
                        artType = doc.getString("artType") ?: "",
                        district = doc.getString("district") ?: "",
                        image = doc.getString("image") ?: "",
                        phone = doc.getString("phone") ?: "",
                        equipment = doc.getString("equipment") ?: "",
                        serviceArea = doc.getString("serviceArea") ?: "",
                        description = doc.getString("description") ?: "",
                        gallery = galleryList
                    )
                    tempList.add(troupe)
                }

                troupeList = tempList  // 🔥 THIS FILLS YOUR UI
            }
    }


    var selectedDistrict by remember { mutableStateOf("All") }
    var selectedArt by remember { mutableStateOf("All") }
    var favorites by remember { mutableStateOf(setOf<String>()) }



    val filteredList = troupeList.filter {
        (selectedDistrict == "All" || it.district == selectedDistrict) &&
                (selectedArt == "All" || it.artType == selectedArt)
    }

    val districtOptions = listOf("All") + troupeList
        .map { it.district }
        .distinct()

    val artOptions = listOf("All") + troupeList
        .map { it.artType }
        .distinct()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFFFF3E0))
        .padding(top = 24.dp)) {

        if (showAddScreen) {
            AddArtistScreen(
                onBack = { showAddScreen = false }
            )
        }

        else if (selectedTroupe == null) {
            Column {
                Button(
                    onClick = { showAddScreen = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("➕ Register Artist")
                }

                Row(modifier = Modifier.padding(8.dp)) {

                    DropdownMenuBox(
                        label = "District",
                        options = districtOptions,
                        selected = selectedDistrict,
                        onSelectedChange = { selectedDistrict = it }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    DropdownMenuBox(
                        label = "Art",
                        options = artOptions,
                        selected = selectedArt,
                        onSelectedChange = { selectedArt = it }
                    )
                }
            LazyColumn {
                items(filteredList) { troupe ->
                    ArtistCard(
                        troupe = troupe,
                        isFavorite = favorites.contains(troupe.name),
                        onFavoriteClick = {
                            favorites = if (favorites.contains(troupe.name)) {
                                favorites - troupe.name
                            } else {
                                favorites + troupe.name
                            }
                        }
                    ) {
                        selectedTroupe = troupe
                    }
                }
            }
            }
        }

        else {
            ProfileScreen(selectedTroupe!!) {
                selectedTroupe = null
            }
        }
    }
}

@Composable
fun ArtistCard(
    troupe: Troupe,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE0B2)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row {
                AsyncImage(
                    model = troupe.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(end = 8.dp),
                    contentScale = ContentScale.Crop
                )

                Column {
                    Text(text = troupe.name, fontSize = 20.sp)

                    Text(
                        text = troupe.artType,
                        color = Color.DarkGray
                    )

                    Text(
                        text = troupe.district,
                        color = Color.Gray
                    )
                }
            }

            // ⭐ Favorite Button
            Text(
                text = if (isFavorite) "⭐" else "☆",
                fontSize = 24.sp,
                modifier = Modifier.clickable {
                    onFavoriteClick()
                }
            )
        }
    }
}

@Composable
fun ProfileScreen(troupe: Troupe, onBack: () -> Unit) {
    var selectedImage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .padding(top = 24.dp)
            .padding(16.dp)
    ) {
        Button(onClick = { onBack() }) {
            Text("⬅ Back")
        }
        AsyncImage(
            model = troupe.image,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = troupe.name, fontSize = 22.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Art Form: ${troupe.artType}",
            color = Color.DarkGray
        )
        Text(text = "District: ${troupe.district}")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "📍 Service Area: ${troupe.serviceArea}")
        Text(text = "🥁 Equipment: ${troupe.equipment}")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "About:")
        Text(text = troupe.description)

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Gallery", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(200.dp)
        ) {
            items(troupe.gallery) { img ->
                AsyncImage(
                    model = img,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .height(100.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            selectedImage = img},
                    contentScale = ContentScale.Crop
                )
            }
        }

        val context = LocalContext.current

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${troupe.phone}")
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD84315)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📞 Book / Call Now")
        }
    }
    selectedImage?.let { imageUrl ->
        Dialog(onDismissRequest = { selectedImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun AddArtistScreen(onBack: () -> Unit) {

    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var artType by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var serviceArea by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())) {

        Button(onClick = onBack) {
            Text("⬅ Back")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        OutlinedTextField(value = artType, onValueChange = { artType = it }, label = { Text("Art Type") })
        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") })
        OutlinedTextField(value = image, onValueChange = { image = it }, label = { Text("Image URL") })
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
        OutlinedTextField(value = equipment, onValueChange = { equipment = it }, label = { Text("Equipment") })
        OutlinedTextField(value = serviceArea, onValueChange = { serviceArea = it }, label = { Text("Service Area") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                val artist = hashMapOf(
                    "name" to name,
                    "artType" to artType,
                    "district" to district,
                    "image" to image,
                    "phone" to phone,
                    "equipment" to equipment,
                    "serviceArea" to serviceArea,
                    "description" to description,

                )

                db.collection("artists")
                    .add(artist)
                    .addOnSuccessListener {
                        Log.d("FIREBASE", "Artist Added ✅")
                        onBack()
                    }
                    .addOnFailureListener {
                        Log.e("FIREBASE", "Error ❌", it)
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Artist")
        }
    }
}

@Composable
    fun DropdownMenuBox(
        label: String,
        options: List<String>,
        selected: String,
        onSelectedChange: (String) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Column {
            Text(text = label)

            Button(onClick = { expanded = true }) {
                Text(selected)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            onSelectedChange(it)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KalavidaraBalagaTheme {
        Greeting("Android")
    }
}