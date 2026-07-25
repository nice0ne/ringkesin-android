package com.msam.ringkesin.data.repository

import com.msam.ringkesin.data.local.dao.SessionDao
import com.msam.ringkesin.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {

    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    fun searchSessions(query: String): Flow<List<SessionEntity>> =
        sessionDao.searchSessions(query)

    suspend fun getSessionById(id: Long): SessionEntity? =
        sessionDao.getSessionById(id)

    suspend fun insert(session: SessionEntity): Long =
        sessionDao.insert(session)

    suspend fun update(session: SessionEntity) =
        sessionDao.update(session)

    suspend fun delete(session: SessionEntity) =
        sessionDao.delete(session)

    suspend fun deleteAll() =
        sessionDao.deleteAll()
}
