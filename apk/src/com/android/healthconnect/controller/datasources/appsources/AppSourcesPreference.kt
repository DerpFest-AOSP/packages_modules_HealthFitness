/**
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.healthconnect.controller.datasources.appsources

import android.content.Context
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.healthconnect.controller.R
import com.android.healthconnect.controller.datasources.DataSourcesFragment
import com.android.healthconnect.controller.datasources.DataSourcesViewModel
import com.android.healthconnect.controller.permissions.connectedapps.ComparablePreference
import com.android.healthconnect.controller.shared.HealthDataCategoryInt
import com.android.healthconnect.controller.shared.app.AppMetadata

class AppSourcesPreference
constructor(
    context: Context,
    private val dataSourcesViewModel: DataSourcesViewModel,
    val category: @HealthDataCategoryInt Int,
    private val fragment: DataSourcesFragment
) : Preference(context), ComparablePreference, AppSourcesAdapter.ItemMoveAttachCallbackListener {

    private var priorityList: List<AppMetadata> = listOf()
    private var potentialAppSourcesList: List<AppMetadata> = listOf()
    private lateinit var priorityListView: RecyclerView
    private lateinit var adapter: AppSourcesAdapter

    init {
        layoutResource = R.layout.widget_linear_layout_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        priorityList = dataSourcesViewModel.getEditedPriorityList()
        potentialAppSourcesList = dataSourcesViewModel.getEditedPotentialAppSources()
        priorityListView = holder.findViewById(R.id.linear_layout_recycle_view) as RecyclerView

        adapter = AppSourcesAdapter(priorityList, potentialAppSourcesList,
            dataSourcesViewModel, category, fragment, this)
        priorityListView.adapter = adapter
        priorityListView.layoutManager = AppSourcesLinearLayoutManager(context, adapter)
        createAndAttachItemMoveCallback()
    }

    override fun attachCallback() {
        createAndAttachItemMoveCallback()
    }

    private fun createAndAttachItemMoveCallback() {
        val callback = AppSourcesItemMoveCallback(adapter)
        val priorityListMover = ItemTouchHelper(callback)
        adapter.setOnItemDragStartedListener(priorityListMover)
        priorityListMover.attachToRecyclerView(priorityListView)
    }

    fun toggleEditMode(isEditMode: Boolean) {
        adapter.toggleEditMode(isEditMode)
    }

    override fun isSameItem(preference: Preference): Boolean {
        return preference is AppSourcesPreference && this == preference
    }

    override fun hasSameContents(preference: Preference): Boolean {
        return preference is AppSourcesPreference &&
                preference.priorityList == this.priorityList &&
                preference.category == this.category
    }
}