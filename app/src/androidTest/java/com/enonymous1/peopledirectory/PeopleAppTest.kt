package com.enonymous1.peopledirectory

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.enonymous1.peopledirectory.data.PeopleDatabase
import com.enonymous1.peopledirectory.data.RoomPeopleRepository
import com.enonymous1.peopledirectory.ui.PeopleApp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on a device or emulator only; the GitHub Actions workflow runs the
 * JVM unit tests and a debug build, not these tests.
 */
@RunWith(AndroidJUnit4::class)
class PeopleAppTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun launch() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PeopleDatabase::class.java).build()
        val viewModel = PeopleViewModel(RoomPeopleRepository(database.peopleDao()))
        composeRule.setContent {
            MaterialTheme { PeopleApp(viewModel) }
        }
    }

    @Test
    fun emptyStateIsShownBeforeAnythingIsAdded() {
        launch()
        composeRule.onNodeWithText("No people yet. Add someone and they will appear here.")
            .assertIsDisplayed()
    }

    @Test
    fun addingAStudentShowsTheirNameAndIdInTheList() {
        launch()
        composeRule.onNodeWithTag("firstNameField").performTextInput("Ada")
        composeRule.onNodeWithTag("lastNameField").performTextInput("Lovelace")
        composeRule.onNodeWithTag("detailField").performTextInput("123")
        composeRule.onNodeWithTag("addButton").performClick()
        composeRule.onNodeWithText("Ada Lovelace").assertIsDisplayed()
        composeRule.onNodeWithText("ID number: 123", substring = true).assertIsDisplayed()
    }

    @Test
    fun emptyFieldsShowValidationErrorsInsteadOfAdding() {
        launch()
        composeRule.onNodeWithTag("addButton").performClick()
        composeRule.onNodeWithText("First name is required.").assertIsDisplayed()
    }
}
