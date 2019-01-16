package com.github.jacklt.githubsample.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BindingHolder<out T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root) {
    constructor(parent: ViewGroup, layout: Int) :
            this(DataBindingUtil.inflate<T>(LayoutInflater.from(parent.context), layout, parent, false))
}