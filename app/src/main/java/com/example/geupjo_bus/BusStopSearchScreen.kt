package com.example.geupjo_bus

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geupjo_bus.api.BusApiClient
import com.example.geupjo_bus.api.BusStopItem
import com.example.geupjo_bus.ui.theme.Geupjo_BusTheme
import kotlinx.coroutines.launch
import java.net.URLDecoder

@Composable
fun BusStopSearchScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    apiKey: String
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var searchResults by remember { mutableStateOf(listOf<String>()) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = onBackClick, modifier = Modifier.align(Alignment.Start)) {
            Text("뒤로 가기")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "정류장 검색", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
            },
            label = { Text("정류장 이름 입력") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    coroutineScope.launch {
                        searchResults = searchBusStopsFromApi(searchQuery.text, apiKey)
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchResults.isNotEmpty()) {
            Text(text = "검색 결과:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            searchResults.forEach { result ->
                BusStopSearchResultItem(busStopName = result)
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text(text = "검색 결과가 없습니다.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun BusStopSearchResultItem(busStopName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(text = busStopName, style = MaterialTheme.typography.titleMedium)
    }
}

// 실제 API 호출 함수
suspend fun searchBusStopsFromApi(query: String, apiKey: String): List<String> {
    return try {
        val decodedKey = URLDecoder.decode(apiKey, "UTF-8")
        val response = BusApiClient.apiService.searchBusStops(
            apiKey = decodedKey,
            cityCode = 38030,
            nodeNm = query
        )

        if (response.isSuccessful) {
            Log.d("Raw XML Response", response.raw().toString())
            Log.d("Parsed XML Response", response.body().toString())
            val bodyResponse: List<BusStopItem>? = response.body()?.body?.items?.itemList
            Log.d("API Response Parsed Items", "Items: $bodyResponse")

            bodyResponse?.forEachIndexed { index, item ->
                Log.d("API Response Item $index", "nodeName: ${item.nodeName}")
            }

            bodyResponse?.mapNotNull { it.nodeName } ?: emptyList()
        } else {
            Log.e("API Error", "API 호출 실패 - 코드: ${response.code()}, 메시지: ${response.message()}")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("API Exception", "API 호출 오류: ${e.message}")
        emptyList()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBusStopSearchScreen() {
    Geupjo_BusTheme {
        BusStopSearchScreen(
            onBackClick = {},
            apiKey = "DUMMY_API_KEY"
        )
    }
}
