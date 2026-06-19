package com.example.spotted.utils

interface MultiplePermissionHandler {
    val statuses: Map<String, com.example.spotted.utils.gps.PermissionStatus>
    fun launchPermissionRequest()
}