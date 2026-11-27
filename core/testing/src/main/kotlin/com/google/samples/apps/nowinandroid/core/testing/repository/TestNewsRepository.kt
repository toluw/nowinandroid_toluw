/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.core.testing.repository

import com.google.samples.apps.nowinandroid.core.data.Synchronizer
import com.google.samples.apps.nowinandroid.core.data.repository.NewsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.NewsResourceQuery
import com.google.samples.apps.nowinandroid.core.model.data.NewsResource
import com.google.samples.apps.nowinandroid.core.model.data.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestNewsRepository : NewsRepository {

    /**
     * Emits once a test seeds or updates news resources.
     */
    private val newsResourcesFlow = MutableStateFlow<List<NewsResource>?>(null)

    override fun getNewsResources(query: NewsResourceQuery): Flow<List<NewsResource>> =
        newsResourcesFlow.filterNotNull().map { newsResources ->
            var result = newsResources
            query.filterTopicIds?.let { filterTopicIds ->
                result = newsResources.filter {
                    it.topics.map(Topic::id).intersect(filterTopicIds).isNotEmpty()
                }
            }
            query.filterNewsIds?.let { filterNewsIds ->
                result = newsResources.filter { it.id in filterNewsIds }
            }
            result
        }

    /**
     * A test-only API to allow controlling the list of news resources from tests.
     */
    fun sendNewsResources(newsResources: List<NewsResource>) {
        newsResourcesFlow.value = newsResources
    }

    override suspend fun updateNewsResource(newsResource: NewsResource) {
        newsResourcesFlow.update { current ->
            val resources = current.orEmpty().associateBy(NewsResource::id).toMutableMap()
            val topics = resources[newsResource.id]?.topics.orEmpty()
            resources[newsResource.id] = newsResource.copy(topics = topics)
            resources.values.toList()
        }
    }

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}
