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

package com.google.samples.apps.nowinandroid.core.data.test.repository

import com.google.samples.apps.nowinandroid.core.common.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.common.network.NiaDispatchers.IO
import com.google.samples.apps.nowinandroid.core.data.Synchronizer
import com.google.samples.apps.nowinandroid.core.data.model.asExternalModel
import com.google.samples.apps.nowinandroid.core.data.repository.NewsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.NewsResourceQuery
import com.google.samples.apps.nowinandroid.core.model.data.NewsResource
import com.google.samples.apps.nowinandroid.core.network.demo.DemoNiaNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Fake implementation of the [NewsRepository] that retrieves the news resources from a JSON String.
 *
 * This allows us to run the app with fake data, without needing an internet connection or working
 * backend.
 */
class FakeNewsRepository @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val datasource: DemoNiaNetworkDataSource,
) : NewsRepository {

    private val updatedNewsResources = MutableStateFlow<Map<String, NewsResource>>(emptyMap())

    override fun getNewsResources(query: NewsResourceQuery): Flow<List<NewsResource>> =
        flow {
            val topics = datasource.getTopics()
            emit(datasource.getNewsResources().map { it.asExternalModel(topics) })
        }.combine(updatedNewsResources) { demoResources, updates ->
            val resources = demoResources.associateBy(NewsResource::id).toMutableMap()
            updates.forEach { (id, newsResource) ->
                resources[id] = newsResource.copy(topics = resources[id]?.topics.orEmpty())
            }
            resources.values.filter { newsResource ->
                query.filterNewsIds?.contains(newsResource.id) != false &&
                    query.filterTopicIds?.let { topicIds ->
                        newsResource.topics.any { it.id in topicIds }
                    } != false
            }
        }.flowOn(ioDispatcher)

    override suspend fun updateNewsResource(newsResource: NewsResource) {
        updatedNewsResources.update { it + (newsResource.id to newsResource) }
    }

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}
