package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.BoardSaathiDatabase
import com.example.data.local.UserPreferencesDataStore
import com.example.data.remote.FirestoreSyncRepository
import com.example.data.repository.StudyRepository
import com.example.ui.BoardSaathiApp
import com.example.ui.theme.BoardSaathiTheme
import com.example.viewmodel.AdminViewModel
import com.example.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContext = applicationContext
        val database = BoardSaathiDatabase.getInstance(appContext)
        val prefsStore = UserPreferencesDataStore(appContext)
        val remoteRepo = FirestoreSyncRepository(appContext)
        val repository = StudyRepository(
            dao = database.dao(),
            prefsStore = prefsStore,
            remote = remoteRepo,
            auth = Firebase.auth
        )

        setContent {
            BoardSaathiTheme {
                val mainViewModel: MainViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { MainViewModel(repository) }
                    }
                )
                val adminViewModel: AdminViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { AdminViewModel(repository) }
                    }
                )
                BoardSaathiApp(
                    mainViewModel = mainViewModel,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}
