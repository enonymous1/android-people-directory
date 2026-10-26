package com.enonymous1.peopledirectory.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.enonymous1.peopledirectory.model.Person
import com.enonymous1.peopledirectory.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * One row per person. The role's unique field (ID number, course, or program)
 * is stored in [detail], so a single table covers all three roles without
 * the key/value-pair storage the original assignment ruled out.
 */
@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roleName: String,
    val firstName: String,
    val lastName: String,
    val detail: String,
) {
    fun toDomain(): Person = when (Role.valueOf(roleName)) {
        Role.STUDENT -> Person.Student(id, firstName, lastName, detail)
        Role.TEACHER -> Person.Teacher(id, firstName, lastName, detail)
        Role.ADMINISTRATOR -> Person.Administrator(id, firstName, lastName, detail)
    }

    companion object {
        fun fromDomain(person: Person): PersonEntity = PersonEntity(
            id = person.id,
            roleName = person.role.name,
            firstName = person.firstName,
            lastName = person.lastName,
            detail = person.detail,
        )
    }
}

@Dao
interface PeopleDao {
    @Query("SELECT * FROM people ORDER BY id ASC")
    fun observeAll(): Flow<List<PersonEntity>>

    @Insert
    suspend fun insert(person: PersonEntity)

    @Delete
    suspend fun delete(person: PersonEntity)
}

@Database(entities = [PersonEntity::class], version = 1, exportSchema = false)
abstract class PeopleDatabase : RoomDatabase() {
    abstract fun peopleDao(): PeopleDao
}

/** The data layer behind [PeopleViewModel]. Kept as an interface so tests can fake it. */
interface PeopleRepository {
    val people: Flow<List<Person>>
    suspend fun add(person: Person)
    suspend fun delete(person: Person)
}

class RoomPeopleRepository(private val dao: PeopleDao) : PeopleRepository {
    override val people: Flow<List<Person>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(person: Person) {
        dao.insert(PersonEntity.fromDomain(person))
    }

    override suspend fun delete(person: Person) {
        dao.delete(PersonEntity.fromDomain(person))
    }
}
