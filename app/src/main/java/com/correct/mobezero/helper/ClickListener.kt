package com.correct.mobezero.helper

import android.os.Bundle

interface ClickListener {
    fun onItemClickListener(position: Int, bundle: Bundle ?= null)
    fun onItemLongClickListener(position: Int, bundle: Bundle ?= null)
}