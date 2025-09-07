package com.drhowdydoo.appinfo.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.drhowdydoo.appinfo.model.Action
import com.drhowdydoo.appinfo.model.ViewState

data class MainViewModel (
    var searchView: Array<ViewState> = arrayOf(
        ViewState(visibility = View.GONE, enabled = true, text = ""),
        ViewState(visibility = View.GONE, enabled = true, text = "")
    ),
    var filterBtn: Array<ViewState> = emptyArray(),
    var sortBtn: Array<ViewState> = emptyArray(),
    var appFilter: MutableLiveData<Int> = MutableLiveData(0),
    var appSort: MutableLiveData<Pair<Int, Boolean>> = MutableLiveData(Pair(0, false)),
    var apkFilter: MutableLiveData<Int> = MutableLiveData(0),
    var apkSort: MutableLiveData<Int> = MutableLiveData(0),
    var pagerPosition: MutableLiveData<Int> = MutableLiveData(0),
    var event: MutableLiveData<Action<*>> = MutableLiveData(null)
) : ViewModel() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MainViewModel) return false

        if (!searchView.contentEquals(other.searchView)) return false
        if (!filterBtn.contentEquals(other.filterBtn)) return false
        if (!sortBtn.contentEquals(other.sortBtn)) return false
        if (appFilter != other.appFilter) return false
        if (appSort != other.appSort) return false
        if (pagerPosition != other.pagerPosition) return false
        if (event != other.event) return false

        return true
    }

    override fun hashCode(): Int {
        var result = searchView.contentHashCode()
        result = 31 * result + filterBtn.contentHashCode()
        result = 31 * result + sortBtn.contentHashCode()
        result = 31 * result + appFilter.hashCode()
        result = 31 * result + appSort.hashCode()
        result = 31 * result + pagerPosition.hashCode()
        result = 31 * result + event.hashCode()
        return result
    }
}
