package com.enonymous1.peopledirectory.model

/**
 * The three roles from the original CE05 app. Each role has one field that is
 * unique to it, on top of the first and last names shared by every person.
 */
enum class Role(val label: String, val detailLabel: String) {
    STUDENT("Student", "ID number"),
    TEACHER("Teacher", "Course"),
    ADMINISTRATOR("Administrator", "Program"),
}

/**
 * A person in the directory. The subclasses carry only their own field;
 * firstName and lastName live here, once, instead of being shadowed in
 * every subclass the way the original Java model did.
 */
sealed class Person {
    abstract val id: Long
    abstract val firstName: String
    abstract val lastName: String
    abstract val role: Role
    abstract val detail: String

    val displayName: String
        get() = "$firstName $lastName"

    data class Student(
        override val id: Long = 0,
        override val firstName: String,
        override val lastName: String,
        val idNumber: String,
    ) : Person() {
        override val role: Role = Role.STUDENT
        override val detail: String get() = idNumber
    }

    data class Teacher(
        override val id: Long = 0,
        override val firstName: String,
        override val lastName: String,
        val course: String,
    ) : Person() {
        override val role: Role = Role.TEACHER
        override val detail: String get() = course
    }

    data class Administrator(
        override val id: Long = 0,
        override val firstName: String,
        override val lastName: String,
        val program: String,
    ) : Person() {
        override val role: Role = Role.ADMINISTRATOR
        override val detail: String get() = program
    }
}

/** Pure validation and construction, so it can be unit-tested without Android. */
object PersonValidation {

    fun validate(role: Role, firstName: String, lastName: String, detail: String): List<String> {
        val errors = mutableListOf<String>()
        if (firstName.isBlank()) errors += "First name is required."
        if (lastName.isBlank()) errors += "Last name is required."
        if (detail.isBlank()) errors += "${role.detailLabel} is required."
        return errors
    }

    /** Returns null when validation fails. Input is trimmed, so whitespace-only text is rejected. */
    fun create(
        id: Long = 0,
        role: Role,
        firstName: String,
        lastName: String,
        detail: String,
    ): Person? {
        if (validate(role, firstName, lastName, detail).isNotEmpty()) return null
        val first = firstName.trim()
        val last = lastName.trim()
        val value = detail.trim()
        return when (role) {
            Role.STUDENT -> Person.Student(id, first, last, value)
            Role.TEACHER -> Person.Teacher(id, first, last, value)
            Role.ADMINISTRATOR -> Person.Administrator(id, first, last, value)
        }
    }
}
