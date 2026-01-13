package com.idloquy.landmark.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idloquy.landmark.data.database.dao.MarkDao
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.model.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandmarkViewModel @Inject constructor(
    private val markDao: MarkDao,
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
        viewModelScope.launch {
            markDao.insertAll(mark)
        }
    }

    fun getMarkById(id: Int): Flow<Mark?> {
        return markDao.getById(id)
    }

    fun updateMark(mark: Mark) {
        viewModelScope.launch {
            markDao.updateAll(mark)
        }
    }

    fun deleteMark(mark: Mark) {
        viewModelScope.launch {
            markDao.deleteAll(mark)
        }
    }

    fun deleteMarks(marks: List<Mark>) {
        viewModelScope.launch {
            markDao.deleteAll(marks)
        }
    }
}