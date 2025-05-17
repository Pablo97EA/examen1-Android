package com.moviles.examen1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moviles.examen1.models.Student
import com.moviles.examen1.models.Course
import com.moviles.examen1.ui.theme.Examen1Theme
import com.moviles.examen1.viewmodel.StudentViewModel
import com.moviles.examen1.services.NotificationService

class StudentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val courseId = intent.getIntExtra("COURSE_ID", -1)
        // Inicializar notificaciones
        NotificationService.initialize(this)
        setContent {
            Examen1Theme {
                StudentsScreen(courseId = courseId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(courseId: Int) {
    val viewModel: StudentViewModel = viewModel()
    val students by viewModel.studentsByCourse.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    val context = LocalContext.current

    LaunchedEffect(showDialog) {
        if (!showDialog) {
            selectedStudent = null
        }
    }

    LaunchedEffect(courseId) {
        if (courseId != -1) {
            viewModel.fetchStudentsByCourse(courseId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estudiantes del Curso $courseId") },
                navigationIcon = {
                    IconButton(onClick = { (context as android.app.Activity).finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedStudent = null
                            showDialog = true
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Student")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isLoading) {
                FloatingActionButton(
                    onClick = {
                        selectedStudent = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Student")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        ) {
            when {
                isLoading && students.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                students.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No hay estudiantes en este curso")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                selectedStudent = null
                                showDialog = true
                            },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar Estudiante")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = students,
                            key = { it.id ?: 0 }
                        ) { student ->
                            StudentCard(
                                student = student,
                                onEdit = {
                                    selectedStudent = student
                                    showDialog = true
                                },
                                onDelete = {
                                    viewModel.deleteStudent(student.id ?: return@StudentCard) {
                                        viewModel.fetchStudentsByCourse(courseId)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (showDialog) {
                StudentDialog(
                    student = selectedStudent,
                    courseId = courseId,
                    onDismiss = {
                        showDialog = false
                        selectedStudent = null
                    },
                    onSave = { student ->
                        if (student.id == null) {
                            viewModel.createStudent(student, {
                                viewModel.fetchStudentsByCourse(courseId)
                            }, context)
                        } else {
                            viewModel.updateStudent(student) {
                                viewModel.fetchStudentsByCourse(courseId)
                            }
                        }
                        showDialog = false
                        selectedStudent = null
                    }
                )
            }
        }
    }
}

@Composable
fun StudentList(
    students: List<Student>,
    onEdit: (Student) -> Unit,
    onDelete: (Student) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(students) { student ->
            StudentCard(
                student = student,
                onEdit = { onEdit(student) },
                onDelete = { onDelete(student) }
            )
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = student.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Email: ${student.email}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Teléfono: ${student.phone}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onEdit) {
                    Text("Editar")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDialog(
    student: Student?,
    courseId: Int,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    // Usar rememberSaveable para mantener el estado durante recomposiciones
    var name by rememberSaveable { mutableStateOf(student?.name ?: "") }
    var email by rememberSaveable { mutableStateOf(student?.email ?: "") }
    var phone by rememberSaveable { mutableStateOf(student?.phone ?: "") }

    // Resetear valores cuando cambia el estudiante
    LaunchedEffect(student) {
        name = student?.name ?: ""
        email = student?.email ?: ""
        phone = student?.phone ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Text(
                text = if (student == null) "Agregar Estudiante" else "Editar Estudiante",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .widthIn(min = 300.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Student(
                            id = student?.id,
                            name = name,
                            email = email,
                            phone = phone,
                            courseId = courseId,
                            course = student?.course ?: Course(null, "", "", "", "", null)
                        )
                    )
                },
                enabled = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StudentsScreenPreview() {
    Examen1Theme {
        StudentsScreen(courseId = 1)
    }
}