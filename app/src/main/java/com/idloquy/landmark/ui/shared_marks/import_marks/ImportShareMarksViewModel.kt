package com.idloquy.landmark.ui.shared_marks.import_marks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idloquy.landmark.data.repository.SharedMarkGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@HiltViewModel
class ImportSharedMarksViewModel @Inject constructor(
    val repository: SharedMarkGroupRepository,
) : ViewModel() {
    fun importMarkGroup(id: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.importMarkGroup(id)
            } catch (e: Exception) {
                onError(e)
                return@launch
            }

            onSuccess()
        }
    }
}