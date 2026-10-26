package com.enonymous1.peopledirectory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.MaterialTheme
import androidx.room.Room
import com.enonymous1.peopledirectory.data.PeopleDatabase
import com.enonymous1.peopledirectory.data.RoomPeopleRepository
import com.enonymous1.peopledirectory.ui.PeopleApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = Room.databaseBuilder(
            applicationContext,
            PeopleDatabase::class.java,
            "people.db",
        ).build()
        val viewModel = ViewModelProvider(
            this,
            PeopleViewModelFactory(RoomPeopleRepository(database.peopleDao())),
        )[PeopleViewModel::class.java]
        setContent {
            MaterialTheme {
                PeopleApp(viewModel)
            }
        }
    }
}
