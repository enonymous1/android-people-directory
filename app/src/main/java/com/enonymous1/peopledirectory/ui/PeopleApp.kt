package com.enonymous1.peopledirectory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.enonymous1.peopledirectory.PeopleViewModel
import com.enonymous1.peopledirectory.model.Person
import com.enonymous1.peopledirectory.model.Role
import kotlinx.coroutines.delay

/**
 * Single screen, adaptive instead of landscape-only: form and list sit
 * side by side on wide screens and stack on narrow ones. The original
 * portrait layout was only a "please rotate" message; this one works
 * in both orientations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleApp(viewModel: PeopleViewModel, modifier: Modifier = Modifier) {
    val people by viewModel.people.collectAsState()

    var role by remember { mutableStateOf(Role.STUDENT) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(emptyList<String>()) }
    var confirmation by remember { mutableStateOf<String?>(null) }
    // Unsaved input is kept per role, so switching roles no longer discards a half-filled form.
    var drafts by remember { mutableStateOf(emptyMap<Role, Triple<String, String, String>>()) }

    LaunchedEffect(confirmation) {
        if (confirmation != null) {
            delay(3000)
            confirmation = null
        }
    }

    fun switchRole(newRole: Role) {
        if (newRole == role) return
        drafts = drafts + (role to Triple(firstName, lastName, detail))
        val draft = drafts[newRole] ?: Triple("", "", "")
        role = newRole
        firstName = draft.first
        lastName = draft.second
        detail = draft.third
        errors = emptyList()
    }

    fun submit() {
        val result = viewModel.addPerson(role, firstName, lastName, detail)
        errors = result
        if (result.isEmpty()) {
            confirmation = "Added ${firstName.trim()} ${lastName.trim()}."
            drafts = drafts - role
            firstName = ""
            lastName = ""
            detail = ""
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("People Directory") }) },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val wide = maxWidth >= 600.dp
            val form = @Composable {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        Role.entries.forEachIndexed { index, entry ->
                            SegmentedButton(
                                selected = role == entry,
                                onClick = { switchRole(entry) },
                                shape = SegmentedButtonDefaults.itemShape(index, Role.entries.size),
                            ) { Text(entry.label) }
                        }
                    }
                    Text(
                        text = "Add a ${role.label.lowercase()}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("firstNameField"),
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("lastNameField"),
                    )
                    OutlinedTextField(
                        value = detail,
                        onValueChange = { detail = it },
                        label = { Text(role.detailLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (role == Role.STUDENT) KeyboardType.Number else KeyboardType.Text,
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("detailField"),
                    )
                    errors.forEach { error ->
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = ::submit,
                        modifier = Modifier.fillMaxWidth().testTag("addButton"),
                    ) { Text("Add ${role.label}") }
                    confirmation?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
                }
            }
            val list = @Composable {
                if (people.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No people yet. Add someone and they will appear here.",
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = people,
                            key = { "${it.role}/${it.id}/${it.firstName}/${it.lastName}/${it.detail}" },
                        ) { person ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = person.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            text = "${person.role.label} · ${person.role.detailLabel}: ${person.detail}",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deletePerson(person) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete ${person.displayName}",
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (wide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(0.45f)) { form() }
                    Column(modifier = Modifier.weight(0.55f)) { list() }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    form()
                    Column(modifier = Modifier.weight(1f)) { list() }
                }
            }
        }
    }
}
