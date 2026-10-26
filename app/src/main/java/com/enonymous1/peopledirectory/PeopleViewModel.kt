package com.enonymous1.peopledirectory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enonymous1.peopledirectory.data.PeopleRepository
import com.enonymous1.peopledirectory.model.Person
import com.enonymous1.peopledirectory.model.PersonValidation
import com.enonymous1.peopledirectory.model.Role
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns the directory state. The list is a live flow from the repository, so
 * adding a person updates the list immediately — the original app's manual
 * Refresh button is no longer needed, and switching roles or rotating the
 * phone no longer discards anything.
 */
class PeopleViewModel(private val repository: PeopleRepository) : ViewModel() {

    val people: StateFlow<List<Person>> = repository.people
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Returns validation errors, or an empty list when the person was queued for saving. */
    fun addPerson(role: Role, firstName: String, lastName: String, detail: String): List<String> {
        val errors = PersonValidation.validate(role, firstName, lastName, detail)
        if (errors.isNotEmpty()) return errors
        val person = PersonValidation.create(role = role, firstName = firstName, lastName = lastName, detail = detail)
            ?: return errors
        viewModelScope.launch { repository.add(person) }
        return emptyList()
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch { repository.delete(person) }
    }
}

class PeopleViewModelFactory(private val repository: PeopleRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeopleViewModel::class.java)) {
            return PeopleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
