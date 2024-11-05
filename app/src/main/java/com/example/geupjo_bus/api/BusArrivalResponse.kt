package com.example.geupjo_bus.api

data class BusArrivalResponse(
    val arrivals: List<BusArrival>
)

data class BusArrival(
    val busNumber: String,
    val arrivalTime: String
)
