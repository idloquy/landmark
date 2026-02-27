package com.idloquy.landmark.ui.shared_marks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idloquy.landmark.data.database.dao.MarkDao
import com.idloquy.landmark.data.database.dao.SharedMarkGroupDao
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.data.database.model.SharedMark
import com.idloquy.landmark.data.database.model.SharedMarkGroup
import com.idloquy.landmark.data.database.model.SharedMarkGroupWithMarks
import com.idloquy.landmark.data.network.LandmarkApiService
import com.idloquy.landmark.data.network.model.ApiResponse
import com.idloquy.landmark.data.network.model.RequestSharedMarkGroup
import com.idloquy.landmark.data.repository.SharedMarkGroupRepository
import com.idloquy.landmark.model.Location
import com.squareup.moshi.JsonDataException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@HiltViewModel
class SharedMarksViewModel @Inject constructor(
    val markDao: MarkDao,
    val repository: SharedMarkGroupRepository,
) : ViewModel() {
    fun getMarks(): Flow<List<Mark>> {
        return markDao.getAll()
    }

    fun getMarkGroups(): Flow<List<SharedMarkGroup>> {
        return repository.getMarkGroups()
    }

    fun getMarkGroupWithMarks(id: String): Flow<SharedMarkGroupWithMarks?> {
        return repository.getMarkGroupWithMarks(id)
    }

    fun refreshMarkGroup(id: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.refreshMarkGroup(id)
            } catch (e: Exception) {
                onError(e)
                return@launch
            }
            onSuccess()
        }
    }

    fun deleteGroups(groups: List<SharedMarkGroup>, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteMarkGroups(groups)
            } catch (e: Exception) {
                onError(e)
                return@launch
            }
            onSuccess()
        }
    }

    fun deleteGroup(group: SharedMarkGroup, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteMarkGroup(group)
            } catch (e: Exception) {
                onError(e)
                return@launch
            }
            onSuccess()
        }
    }

    fun deleteMarks(group: SharedMarkGroup, marks: List<SharedMark>, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteSharedMarks(group, marks)
            } catch (e: Exception) {
                onError(e)
                return@launch
            }
            onSuccess()
        }
    }

    fun addMarks(group: SharedMarkGroup, marks: List<Mark>, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addMarks(group, marks)
            } catch (e: Exception) {
                onError(e)
                return@launch
            }
            onSuccess()
        }
    }

    fun importEditToken(group: SharedMarkGroup, editToken: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.importEditToken(group, editToken)
            } catch (e: Exception) {
                onError(e)
                return@launch
            }
            onSuccess()
        }
    }
}