package com.tukorea.bus.ui.temp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.data.model.TempResponse
import com.tukorea.bus.data.repository.TempRepository
import com.tukorea.bus.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TempViewModel @Inject constructor(
    private val repo: TempRepository
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<TempResponse>>>(Resource.Loading)
    val state: StateFlow<Resource<List<TempResponse>>> = _state

    fun loadTemps() {
        viewModelScope.launch {
            _state.value = Resource.Loading
            _state.value = repo.getTemps()
        }
    }
}