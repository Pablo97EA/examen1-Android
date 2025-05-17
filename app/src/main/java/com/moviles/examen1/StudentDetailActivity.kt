package com.moviles.examen1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.moviles.examen1.models.Student
import com.moviles.examen1.network.RetrofitInstance
import com.moviles.examen1.ui.theme.Examen1Theme
import kotlinx.coroutines.launch

class StudentDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val studentId = intent.getIntExtra("STUDENT_ID", -1)

        setContent {
            Examen1Theme {
                if (studentId != -1) {
                    StudentDetailScreen(studentId)
                } else {
                    Text("ID de estudiante no válido")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun StudentDetailScreen(studentId: Int) {
        var student by remember { mutableStateOf<Student?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        LaunchedEffect(studentId) {
            scope.launch {
                try {
                    isLoading = true
                    student = RetrofitInstance.api.getStudentById(studentId)
                } catch (e: Exception) {
                    error = e.message
                } finally {
                    isLoading = false
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle del Estudiante") }
                )
            }
        ) { padding ->
            Box(modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
            ) {
                when {
                    isLoading -> CircularProgressIndicator()
                    error != null -> Text("Error: $error")
                    student != null -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Nombre: ${student!!.name}")
                            Text("Curso: ${student?.courseName ?: "Curso no asignado"}")
                            Text("Correo: ${student!!.email ?: "No disponible"}")
                        }
                    }
                }
            }
        }
    }

}