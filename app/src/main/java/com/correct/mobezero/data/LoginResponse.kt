package com.correct.mobezero.data

data class LoginResponse(
    val balance_link: String,
    val cdr: String,
    val ex2proxy: String,
    val footer: String,
    val header: String,
    val id: String,
    val ivr: String,
    val message: String,
    val minute_flexibility_link: String,
    val operator_code: String,
    val pkey: String,
    val proxy2ex: String,
    val proxy_encryption: String,
    val proxy_ip: String,
    val register: String,
    val response_code: Int,
    val rreg: String,
    val show_advertisement: String,
    val test_user: String,
    val vpn_minimum_balance: String,
    val vpn_password: String,
    val vpn_server: String,
    val vpn_username: String
)