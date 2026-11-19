package com.snhrnck.calmlock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow // Correct import
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snhrnck.calmlock.data.AccentSage
import com.snhrnck.calmlock.data.AccentSand
import com.snhrnck.calmlock.data.BgSlate
import com.snhrnck.calmlock.data.TextMuted
import kotlinx.coroutines.flow.filter

@Composable
fun WheelNumberPicker(
    label: String,
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit
) {
    val items = range.toList()
    val initialIndex = items.indexOf(initialValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val itemHeight = 45.dp

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collect {
                val currentIndex = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset

                val targetIndex = if (offset > 60) {
                    (currentIndex + 1).coerceAtMost(items.lastIndex)
                } else {
                    currentIndex
                }

                if (targetIndex != listState.firstVisibleItemIndex) {
                    listState.animateScrollToItem(targetIndex)
                }
                onValueChange(items[targetIndex])
            }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width(100.dp)
                .height(135.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BgSlate),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(AccentSage.copy(alpha = 0.2f))
            )

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = itemHeight), // Padding top and bottom
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                items(items.size) { index ->
                    val number = items[index]
                    val isCenter = index == listState.firstVisibleItemIndex &&
                            listState.firstVisibleItemScrollOffset < 60

                    Box(
                        modifier = Modifier.height(itemHeight).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString().padStart(2, '0'),
                            color = if (isCenter) AccentSand else TextMuted.copy(alpha = 0.4f),
                            fontSize = if (isCenter) 28.sp else 20.sp,
                            fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}