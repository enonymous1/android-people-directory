package com.enonymous1.peopledirectory

import com.enonymous1.peopledirectory.model.Person
import com.enonymous1.peopledirectory.model.PersonValidation
import com.enonymous1.peopledirectory.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonValidationTest {

    @Test
    fun blankFirstNameIsAnError() {
        assertEquals(
            listOf("First name is required."),
            PersonValidation.validate(Role.STUDENT, "", "Lovelace", "123"),
        )
    }

    @Test
    fun whitespaceOnlyFieldsAreRejected() {
        val errors = PersonValidation.validate(Role.TEACHER, "   ", "  ", " ")
        assertEquals(3, errors.size)
    }

    @Test
    fun detailErrorUsesTheRoleLabel() {
        assertEquals(
            listOf("Course is required."),
            PersonValidation.validate(Role.TEACHER, "Ada", "Lovelace", ""),
        )
        assertEquals(
            listOf("Program is required."),
            PersonValidation.validate(Role.ADMINISTRATOR, "Ada", "Lovelace", ""),
        )
    }

    @Test
    fun validInputHasNoErrors() {
        assertTrue(
            PersonValidation.validate(Role.STUDENT, "Ada", "Lovelace", "123").isEmpty(),
        )
    }

    @Test
    fun createTrimsAndKeepsTheRoleSpecificField() {
        val person = PersonValidation.create(
            role = Role.STUDENT,
            firstName = "  Ada ",
            lastName = " Lovelace ",
            detail = " 123 ",
        )
        assertTrue(person is Person.Student)
        assertEquals("Ada", person?.firstName)
        assertEquals("Lovelace", person?.lastName)
        assertEquals("123", (person as Person.Student).idNumber)
        // The unique field is reachable now; the original model hid it behind no getters.
        assertEquals("123", person.detail)
    }

    @Test
    fun createReturnsNullForInvalidInput() {
        assertNull(
            PersonValidation.create(Role.TEACHER, "Ada", "", "Math"),
        )
    }

    @Test
    fun teacherAndAdministratorKeepTheirOwnFields() {
        val teacher = PersonValidation.create(Role.TEACHER, "Ada", "Lovelace", "Math") as Person.Teacher
        val admin = PersonValidation.create(Role.ADMINISTRATOR, "Ada", "Lovelace", "CS") as Person.Administrator
        assertEquals("Math", teacher.course)
        assertEquals("CS", admin.program)
        assertEquals("Ada Lovelace", teacher.displayName)
    }
}
