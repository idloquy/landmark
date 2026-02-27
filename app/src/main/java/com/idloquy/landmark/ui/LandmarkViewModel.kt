package com.idloquy.landmark.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idloquy.landmark.data.database.dao.MarkDao
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.data.database.model.SharedMark
import com.idloquy.landmark.data.database.model.SharedMarkGroup
import com.idloquy.landmark.data.repository.SharedMarkGroupRepository
import com.idloquy.landmark.di.IoDispatcher
import com.idloquy.landmark.di.IoScope
import com.idloquy.landmark.model.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandmarkViewModel @Inject constructor(
    private val markDao: MarkDao,
    private val sharedMarkRepository: SharedMarkGroupRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _location: MutableStateFlow<Location?> = MutableStateFlow(null)
    val location = _location.asStateFlow()

    fun updateLocation(newLocation: Location) {
        _location.update { newLocation }
    }

    val marks = markDao.getAll()

    fun markLocation(location: Location, description: String) {
        val mark = Mark(
            location = location,
            description = description,
        )
        viewModelScope.launch(ioDispatcher) {
            markDao.insertAll(mark)
        }
    }

    fun getMarkById(id: Int): Flow<Mark?> {
        return markDao.getById(id)
    }


    fun getSharedMarkById(groupId: String, id: Int): Flow<SharedMark?> {
        return sharedMarkRepository.getMark(groupId, id)
    }

    fun getSharedMarkGroupById(id: String): Flow<SharedMarkGroup?> {
        return sharedMarkRepository.getMarkGroup(id)
    }

    fun updateMark(mark: Mark) {
        viewModelScope.launch(ioDispatcher) {
            markDao.updateAll(mark)
        }
    }

    fun updateMarkForGroup(
        group: SharedMarkGroup,
        mark: SharedMark,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                sharedMarkRepository.updateSharedMark(group, mark)
            } catch (e: Exception) {
                onError(e)
                return@launch
            }

            onSuccess()
        }
    }

    fun deleteMark(mark: Mark, onSuccess: () -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            markDao.deleteAll(mark)
            onSuccess()
        }
    }

    fun deleteMarkForGroup(
        group: SharedMarkGroup,
        mark: SharedMark,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                sharedMarkRepository.deleteSharedMark(group, mark)
            } catch (e: Exception) {
                onError(e)
                return@launch
            }

            onSuccess()
        }
    }

    fun deleteMarks(marks: List<Mark>) {
        viewModelScope.launch(ioDispatcher) {
            markDao.deleteAll(marks)
        }
    }
}