package com.example.ergosense.model

data class LogEvent(
    val title: String,
    val severity: String,
    val detail: String,
    val time: String,
    val description: String,
    val type: String,
    val exposureTime: String = "",
    val threshold: String = "",
    val diagnosis: String = "",
    val correctiveAction: String = "",
    val actionStatus: String = "",
    val dateGroup: String = "Hoy"
)