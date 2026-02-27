package com.idloquy.landmark.ui.shared_marks.share_marks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idloquy.landmark.data.database.dao.MarkDao
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.data.repository.SharedMarkGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareMarksViewModel @Inject constructor(
    val markDao: MarkDao,
    val repository: SharedMarkGroupRepository,
) : ViewModel() {
    fun getAllMarks(): Flow<List<Mark>> {
        return markDao.getAll()
    }

    fun shareMarks(groupName: String, marks: List<Mark>, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            val id = try {
                repository.shareMarkGroup(groupName, marks)
            } catch(e: Exception) {
                onError(e)
                return@launch
            }

            Log.d("landmark", "setting mark group id to $id")
            onSuccess(id)
        }
    }
}