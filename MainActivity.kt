package com.example.lost

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

data class LostFoundItem(
    val id: Int,
    val type: String,
    val name: String,
    val phone: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String,
    val imageUri: String,
    val timestamp: String
)

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "lost_found.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT,
                name TEXT,
                phone TEXT,
                description TEXT,
                date TEXT,
                location TEXT,
                category TEXT,
                imageUri TEXT,
                timestamp TEXT
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS items")
        onCreate(db)
    }

    fun insertItem(item: LostFoundItem): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("type", item.type)
            put("name", item.name)
            put("phone", item.phone)
            put("description", item.description)
            put("date", item.date)
            put("location", item.location)
            put("category", item.category)
            put("imageUri", item.imageUri)
            put("timestamp", item.timestamp)
        }
        return db.insert("items", null, values) != -1L
    }

    fun getAllItems(): List<LostFoundItem> {
        val list = mutableListOf<LostFoundItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM items", null)

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    LostFoundItem(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
                        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return list.sortedByDescending {
            try {
                sdf.parse(it.date)
            } catch (e: ParseException) {
                null
            }
        }
    }

    fun deleteItem(id: Int): Boolean {
        val db = writableDatabase
        return db.delete("items", "id=?", arrayOf(id.toString())) > 0
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LostFoundApp()
        }
    }
}

@Composable
fun LostFoundApp() {
    var screen by remember { mutableStateOf("home") }
    var selectedItem by remember { mutableStateOf<LostFoundItem?>(null) }

    MaterialTheme {
        when (screen) {
            "home" -> HomeScreen(
                onCreateClick = { screen = "create" },
                onShowClick = { screen = "list" }
            )

            "create" -> CreateAdvertScreen(
                onBack = { screen = "home" }
            )

            "list" -> ItemListScreen(
                onBack = { screen = "home" },
                onItemClick = {
                    selectedItem = it
                    screen = "detail"
                }
            )

            "detail" -> selectedItem?.let {
                ItemDetailScreen(
                    item = it,
                    onBack = { screen = "list" },
                    onRemove = {
                        selectedItem = null
                        screen = "list"
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onCreateClick: () -> Unit,
    onShowClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lost & Found App",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onCreateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create New Advert")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onShowClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Lost & Found Items")
            }
        }
    }
}

@Composable
fun CreateAdvertScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { DatabaseHelper(context) }

    var type by remember { mutableStateOf("Lost") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val categories = listOf("Electronics", "Pets", "Wallets", "Documents", "Keys", "Other")

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "Create New Advert",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Post Type", fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = type == "Lost",
                    onClick = { type = "Lost" }
                )
                Text("Lost")

                Spacer(modifier = Modifier.width(20.dp))

                RadioButton(
                    selected = type == "Found",
                    onClick = { type = "Found" }
                )
                Text("Found")
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date, example: 12/05/2026") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Category", fontWeight = FontWeight.Bold)

            categories.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = category == it,
                        onClick = { category = it }
                    )
                    Text(it)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload Image")
            }

            imageUri?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Uploaded image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    when {
                        name.isBlank() -> {
                            Toast.makeText(context, "Enter name", Toast.LENGTH_SHORT).show()
                        }

                        phone.isBlank() -> {
                            Toast.makeText(context, "Enter phone number", Toast.LENGTH_SHORT).show()
                        }

                        description.isBlank() -> {
                            Toast.makeText(context, "Enter description", Toast.LENGTH_SHORT).show()
                        }

                        date.isBlank() -> {
                            Toast.makeText(context, "Enter date", Toast.LENGTH_SHORT).show()
                        }

                        location.isBlank() -> {
                            Toast.makeText(context, "Enter location", Toast.LENGTH_SHORT).show()
                        }

                        imageUri == null -> {
                            Toast.makeText(context, "You must upload an image", Toast.LENGTH_LONG).show()
                        }

                        else -> {
                            val timestamp = SimpleDateFormat(
                                "dd/MM/yyyy HH:mm",
                                Locale.getDefault()
                            ).format(Date())

                            val item = LostFoundItem(
                                id = 0,
                                type = type,
                                name = name,
                                phone = phone,
                                description = description,
                                date = date,
                                location = location,
                                category = category,
                                imageUri = imageUri.toString(),
                                timestamp = timestamp
                            )

                            db.insertItem(item)

                            Toast.makeText(
                                context,
                                "Advertisement saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
fun ItemListScreen(
    onBack: () -> Unit,
    onItemClick: (LostFoundItem) -> Unit
) {
    val context = LocalContext.current
    val db = remember { DatabaseHelper(context) }

    val items = remember { mutableStateOf(db.getAllItems()) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Electronics", "Pets", "Wallets", "Documents", "Keys", "Other")

    val filteredItems = if (selectedCategory == "All") {
        items.value
    } else {
        items.value.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Lost & Found Items",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Filter by Category", fontWeight = FontWeight.Bold)

        categories.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedCategory == it,
                    onClick = { selectedCategory = it }
                )
                Text(it)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredItems.isEmpty()) {
            Text("No items found")
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onItemClick(item) },
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUri),
                            contentDescription = "Item image",
                            modifier = Modifier
                                .size(80.dp)
                                .padding(end = 12.dp)
                        )

                        Column {
                            Text(
                                text = "${item.type}: ${item.name}",
                                fontWeight = FontWeight.Bold
                            )
                            Text("Category: ${item.category}")
                            Text("Date: ${item.date}")
                            Text("Location: ${item.location}")
                            Text("Posted: ${item.timestamp}")
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
fun ItemDetailScreen(
    item: LostFoundItem,
    onBack: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { DatabaseHelper(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Item Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = rememberAsyncImagePainter(item.imageUri),
            contentDescription = "Item image",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Type: ${item.type}")
        Text("Name: ${item.name}")
        Text("Phone: ${item.phone}")
        Text("Description: ${item.description}")
        Text("Date: ${item.date}")
        Text("Location: ${item.location}")
        Text("Category: ${item.category}")
        Text("Posted: ${item.timestamp}")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                db.deleteItem(item.id)

                Toast.makeText(
                    context,
                    "Advertisement removed",
                    Toast.LENGTH_SHORT
                ).show()

                onRemove()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Remove")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}