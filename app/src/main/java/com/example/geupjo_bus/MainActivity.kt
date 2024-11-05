package com.example.geupjo_bus

import com.example.geupjo_bus.BusStopSearchScreen
import java.net.URLDecoder
import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.geupjo_bus.ui.theme.Geupjo_BusTheme
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.geupjo_bus.api.BusApiClient
import com.example.geupjo_bus.api.BusStop
import com.example.geupjo_bus.api.BusArrival
import com.google.accompanist.permissions.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.util.Log
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class) // Accompanist 경고 처리
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Geupjo_BusTheme {
                var drawerState by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("home") } // 현재 화면 상태 관리
                val scope = rememberCoroutineScope()

                Box(Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("진주시 버스 정보") },
                                actions = {
                                    IconButton(onClick = {
                                        drawerState = true
                                    }) {
                                        Text("메뉴")
                                    }
                                },
                                colors = TopAppBarDefaults.smallTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        },
                        content = { innerPadding ->
                            // 화면 전환 로직
                            when (currentScreen) {
                                "home" -> BusAppContent(
                                    Modifier.padding(innerPadding),
                                    onSearchClick = { currentScreen = "search" }, // 검색 화면으로 전환
                                    onRouteSearchClick = { currentScreen = "route" } // 경로 검색 화면으로 전환
                                )
                                "search" -> BusStopSearchScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onBackClick = { currentScreen = "home" },
                                    apiKey = "cvmPJ15BcYEn%2FRGNukBqLTRlCXkpITZSc6bWE7tWXdBSgY%2FeN%2BvzxH%2FROLnXu%2BThzVwBc09xoXfTyckHj1IJdg%3D%3D" // 실제 인증 키를 여기에 입력
                                )
                                "route" -> RouteSearchScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onBackClick = { currentScreen = "home" } // 홈 화면으로 돌아가기
                                )
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = drawerState,
                        enter = slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(500)
                        ),
                        exit = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(500)
                        )
                    ) {
                        DrawerContent(
                            onDismiss = { drawerState = false },
                            onMenuItemClick = { screen ->
                                currentScreen = screen // 메뉴 클릭 시 화면 전환
                                drawerState = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BusAppContent(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onRouteSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var busStops by remember { mutableStateOf<List<BusStop>>(emptyList()) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    // 현재 위치 가져오기 로직
    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            getCurrentLocation(context, fusedLocationClient) { lat, lng ->
                latitude = lat
                longitude = lng
                // 주변 정류장 정보 가져오기
                coroutineScope.launch {
                    try {
                        // 서비스 키를 URL 디코딩하여 변수에 저장
                        val encodedKey = "cvmPJ15BcYEn%2FRGNukBqLTRlCXkpITZSc6bWE7tWXdBSgY%2FeN%2BvzxH%2FROLnXu%2BThzVwBc09xoXfTyckHj1IJdg%3D%3D"
                        val apiKey = URLDecoder.decode(encodedKey, "UTF-8")

                        // API 호출
                        val response = BusApiClient.apiService.getNearbyBusStops(
                            apiKey = apiKey,
                            latitude = latitude!!,
                            longitude = longitude!!
                        )

                        if (response.isSuccessful) {
                            // 성공한 경우 응답 본문 출력
                            val responseBody = response.body()
                            busStops = responseBody?.body?.items?.itemList?.take(4) ?: emptyList()  // 최대 4개의 정류장만 표시
                            Log.d("API Response", "정류장 목록: $busStops")

                            // 전체 응답의 Raw 데이터 출력
                            Log.d("API Response Raw", response.raw().toString())
                        } else {
                            // 실패한 경우 오류 코드와 메시지 출력
                            val errorBody = response.errorBody()?.string()
                            Log.e("API Error", "API 호출 실패 - 코드: ${response.code()}, 메시지: ${response.message()}")
                            Log.e("API Error Body", "오류 응답 본문: $errorBody")
                        }
                    } catch (e: Exception) {
                        // 예외 발생 시 오류 메시지 출력
                        Log.e("API Error", "정류장 목록을 가져오는데 실패했습니다. ${e.message}")
                    }
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "GPS 기반 주변 정류장 목록:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (busStops.isNotEmpty()) {
            busStops.forEach { busStop ->
                NearbyBusStop(
                    busStopName = busStop.nodeName ?: "알 수 없음", // nodeName이 null이면 "알 수 없음"으로 기본값 제공
                    distance = busStop.nodeNumber ?: "알 수 없음"  // nodeNumber가 null이면 "알 수 없음"으로 기본값 제공
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text("주변 정류장 정보를 불러오는 중입니다...")
        }
    }

        Spacer(modifier = Modifier.height(16.dp))

        //Text(
            //text = "현재 위치 주변 정류장 도착 정보:",
            //style = MaterialTheme.typography.titleMedium,
            //modifier = Modifier.padding(bottom = 8.dp)
        //)

        //if (busArrivals.isNotEmpty()) {
            //busArrivals.forEach { busArrival ->
               // BusArrivalInfo(busNumber = busArrival.busNumber, arrivalTime = busArrival.arrivalTime)
                //Spacer(modifier = Modifier.height(8.dp))
            //}
        //} else {
           // Text("버스 도착 정보를 불러오는 중입니다...")
        //}
    }


@Composable
fun NearbyBusStop(busStopName: String, distance: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(text = busStopName, style = MaterialTheme.typography.titleMedium)
        Text(text = distance, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun BusArrivalInfo(busNumber: String, arrivalTime: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(text = busNumber, style = MaterialTheme.typography.titleMedium)
        Text(text = arrivalTime, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

// 현재 위치를 가져오는 함수
fun getCurrentLocation(
    context: android.content.Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationRetrieved: (Double, Double) -> Unit
) {
    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.d("Location", "위도: $latitude, 경도: $longitude")
                    onLocationRetrieved(latitude, longitude)
                } else {
                    Log.d("Location", "위치를 가져올 수 없습니다.")
                    Toast.makeText(context, "위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

@Composable
fun DrawerContent(onDismiss: () -> Unit, onMenuItemClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text(
            text = "닫기",
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        DrawerMenuItem(label = "홈", onClick = { onMenuItemClick("home") })
        DrawerMenuItem(label = "정류장 검색", onClick = { onMenuItemClick("search") })
        DrawerMenuItem(label = "경로 검색", onClick = { onMenuItemClick("route") }) // 경로 검색 추가
    }
}

@Composable
fun DrawerMenuItem(label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBusAppContent() {
    Geupjo_BusTheme {
        BusAppContent(onSearchClick = {}, onRouteSearchClick = {})
    }
}
