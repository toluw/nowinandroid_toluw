/*
 * Copyright 2026 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.feature.bookmarks.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.model.data.updateNote
import com.google.samples.apps.nowinandroid.core.ui.UserNewsResourcePreviewParameterProvider

@Composable
fun ViewNoteBottomSheet(
    modifier: Modifier = Modifier,
    userNewsResource: UserNewsResource,
    onNewsResourceUpdated: (UserNewsResource) -> Unit,
){

    val textFieldState = rememberTextFieldState(initialText = userNewsResource.note)

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp)
    ){
        TextField(state = textFieldState, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))


        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = textFieldState.text.isNotEmpty(),
            onClick = {
              onNewsResourceUpdated(userNewsResource.updateNote(textFieldState.text.toString()))
            }
        ) {
            Text(if(userNewsResource.note.isEmpty()) "Add Note" else "Update Note")
        }
    }

}


@Preview
@Composable
fun ViewNoteBottomSheetPreview(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
){

   ViewNoteBottomSheet(modifier = Modifier, userNewsResource = userNewsResources[0]) { }

}