package com.enonymous1.peopledirectory

import com.enonymous1.peopledirectory.data.PeopleRepository
import com.enonymous1.peopledirectory.model.Person
import com.enonymous1.peopledirectory.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakePeopleRepository : PeopleRepository {
    private val state = MutableStateFlow<List<Person>>(emptyList())
    override val people: Flow<List<Person>> = state
    private var nextId = 1L

    override suspend fun add(person: Person) {
        val withId = when (person) {
            is Person.Student -> person.copy(id = nextId++)
            is Person.Teacher -> person.copy(id = nextId++)
            is Person.Administrator -> person.copy(id = nextId++)
        }
        state.value = state.value + withId
    }

    override suspend fun delete(person: Person) {
        state.value = state.value.filterNot { it.id == person.id }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PeopleViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addingAValidPersonAppearsInTheList() = runTest {
        val viewModel = PeopleViewModel(FakePeopleRepository())
        assertTrue(viewModel.addPerson(Role.STUDENT, "Ada", "Lovelace", "123").isEmpty())
        advanceUntilIdle()
        assertEquals(1, viewModel.people.value.size)
        val person = viewModel.people.value.single()
        assertEquals("Ada Lovelace", person.displayName)
        assertEquals("123", person.detail)
    }

    @Test
    fun invalidAddReturnsErrorsAndAddsNothing() = runTest {
        val viewModel = PeopleViewModel(FakePeopleRepository())
        val errors = viewModel.addPerson(Role.TEACHER, "Ada", "", "Math")
        assertTrue(errors.isNotEmpty())
        advanceUntilIdle()
        assertTrue(viewModel.people.value.isEmpty())
    }

    @Test
    fun deletingRemovesOnlyThatPerson() = runTest {
        val viewModel = PeopleViewModel(FakePeopleRepository())
        viewModel.addPerson(Role.STUDENT, "Ada", "Lovelace", "123")
        viewModel.addPerson(Role.TEACHER, "Grace", "Hopper", "Compilers")
        advanceUntilIdle()
        assertEquals(2, viewModel.people.value.size)
        viewModel.deletePerson(viewModel.people.value.first())
        advanceUntilIdle()
        assertEquals(listOf("Grace Hopper"), viewModel.people.value.map { it.displayName })
    }
}
