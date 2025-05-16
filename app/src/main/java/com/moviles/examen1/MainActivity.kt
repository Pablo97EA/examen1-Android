package com.moviles.examen1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moviles.examen1.models.Course
import com.moviles.examen1.ui.theme.Examen1Theme
import com.moviles.examen1.viewmodel.CourseViewModel
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.moviles.examen1.common.Constants
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Examen1Theme {
                val viewModel: CourseViewModel = viewModel()
                CourseScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(viewModel: CourseViewModel) {
    val courses by viewModel.courses.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    // Correct method call
    LaunchedEffect(Unit) {
        viewModel.fetchCourses()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Courses") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedCourse = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Course")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                onClick = { viewModel.fetchCourses() } // fixed method name
            ) {
                Text("Refresh Courses")
            }

            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(courses) { course ->
                    CourseItem(
                        course,
                        onEdit = {
                            selectedCourse = course
                            showDialog = true
                        },
                        onDelete = {
                            viewModel.deleteCourse(course.id)
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        CourseDialog(
            course = selectedCourse,
            onDismiss = { showDialog = false },
            onSave = { course, uri ->
                Log.i("CourseScreen", "onSave called with course=$course, uri=$uri")

                if (course.id == null) {
                    // Si no tiene id, es nuevo curso
                    viewModel.addCourse(course, uri)
                } else {
                    // Ya tiene id, actualizamos
                    viewModel.updateCourse(course, uri)
                }

                showDialog = false
            }
        )

    }
}

@Composable
fun CourseItem(course: Course, onEdit: (Course) -> Unit, onDelete: (Course) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Solo muestra imagen si existe imageUrl
            if (!course.imageUrl.isNullOrBlank()) {
                val imageUrl = Constants.API_BASE_URL + course.imageUrl.removePrefix("/")
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Course Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(bottom = 8.dp)
                )
            }

            Text(text = course.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "📚 ${course.description}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "🧑‍🏫 ${course.professor}", style = MaterialTheme.typography.bodySmall)
            Text(text = "📅 ${course.schedule}", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onEdit(course) }) {
                    Text("Edit")
                }
                TextButton(onClick = { onDelete(course) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDialog(course: Course?, onDismiss: () -> Unit, onSave: (Course, Uri?) -> Unit) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var description by remember { mutableStateOf(course?.description ?: "") }
    var professor by remember { mutableStateOf(course?.professor ?: "") }
    var schedule by remember { mutableStateOf(course?.schedule ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (course == null) "Add Course" else "Edit Course") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = professor, onValueChange = { professor = it }, label = { Text("Professor") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = schedule, onValueChange = { schedule = it }, label = { Text("Schedule") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))

                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Select Image")
                }

                imageUri?.let {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(it).build(),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedCourse = Course(
                        id = course?.id,
                        name = name,
                        schedule = schedule,
                        description = description,
                        professor = professor,
                        imageUrl = null // no se usa directamente, será procesado por la lógica con `Uri`
                    )
                    onSave(updatedCourse, imageUri)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Preview(showBackground = true)
@Composable
fun CourseScreenPreview() {
    Examen1Theme {
        val dummyCourse = Course(1, "Math 101", "Mon/Wed 10am", "Basic math concepts", "Prof. Smith", null)
        CourseItem(dummyCourse, onEdit = {}, onDelete = {})
    }
}
