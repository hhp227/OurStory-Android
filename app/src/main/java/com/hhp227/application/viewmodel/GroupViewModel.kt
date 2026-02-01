package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.hhp227.application.R
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.GroupType
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class GroupViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    val state = MutableLiveData(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "GroupViewModel onCleared")
    }

    private fun setPagingData(pagingData: PagingData<GroupItem>?) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    fun onCreateGroup() {

    }

    fun onDeleteGroup(group: GroupItem.Group) {
        val pagingData = state.value?.pagingData?.filter { (it as? GroupItem.Group)?.id != group.id }

        setPagingData(pagingData)
    }

    fun refresh() {
        repository.clearCache(GroupType.Joined)
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow
                .flatMapConcat { repository.getGroupList(it?.apiKey ?: "", GroupType.Joined) }
                .catch { e ->
                    state.value = state.value?.copy(message = e.message)
                }
                .map {
                    it.insertSeparators { first, _ ->
                        if (first == null) {
                            return@insertSeparators GroupItem.Title(R.string.joined_group)
                        }
                        return@insertSeparators null
                    }
                }
                .cachedIn(viewModelScope)
                .collectLatest(::setPagingData)
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val pagingData: PagingData<GroupItem>? = PagingData.empty(),
        val message: String? = ""
    )
}