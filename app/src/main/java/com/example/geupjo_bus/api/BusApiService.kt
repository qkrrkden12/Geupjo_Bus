package com.example.geupjo_bus.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BusApiService {
    // 주변 정류장 정보 가져오기
    @GET("1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList")
    suspend fun getNearbyBusStops(
        @Query("serviceKey") apiKey: String = "cvmPJ15BcYEn%252FRGNukBqLTRlCXkpITZSc6bWE7tWXdBSgY%252FeN%252BvzxH%252FROLnXu%252BThzVwBc09xoXfTyckHj1IJdg%253D%253D%26",
        @Query("gpsLati") latitude: Double,
        @Query("gpsLong") longitude: Double
    ): Response<BusStopResponse>

    // 버스 도착 정보 가져오기
    //@GET("busArrivalInfo")
    //suspend fun getBusArrivalInfo(
    //@Query("serviceKey") apiKey: String = "yY52HVXXDhIkvRCoY8XPGoLBLjZ52jQv%2Br1P%2BA%2Ft9izCnDlOIJZVxSvljQx0yAyAWA4Vz9YnKwPTodc9lZriAw%3D%3D",  // 인증키 추가
    //@Query("busStopId") busStopId: String
    //): BusArrivalResponse

    @GET("1613000/BusSttnInfoInqireService/getSttnNoList")
    suspend fun searchBusStops(
        @Query("serviceKey") apiKey: String = "cvmPJ15BcYEn%252FRGNukBqLTRlCXkpITZSc6bWE7tWXdBSgY%252FeN%252BvzxH%252FROLnXu%252BThzVwBc09xoXfTyckHj1IJdg%253D%253D%26",
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("pageNo") pageNo: Int = 1,
        @Query("cityCode") cityCode: Int,
        @Query("nodeNm") nodeNm: String
    ): Response<BusStopSearchResponse>
}