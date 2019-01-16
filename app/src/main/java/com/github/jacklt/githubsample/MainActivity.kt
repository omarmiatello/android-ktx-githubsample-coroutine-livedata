package com.github.jacklt.githubsample

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.github.jacklt.githubsample.data.RepositoryItem
import com.github.jacklt.githubsample.databinding.ActivityMainBinding
import com.github.jacklt.githubsample.databinding.ItemRepositoryBinding
import com.github.jacklt.githubsample.utils.BindingHolder
import com.github.jacklt.githubsample.utils.openUrlInCustomTab
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    val repositoriesViewModel get() = ViewModelProviders.of(this).get(OrganizationRepositoriesViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServicesConfig.buildInstance()
        val model = repositoriesViewModel

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            viewModel = model
            handler = ActivityHandler()
            setLifecycleOwner(this@MainActivity)

            val adapter = RepositoryAdapter()
            recyclerView.adapter = adapter

            model.bestReposList.observe(this@MainActivity, Observer { adapter.items = it })
        }
    }

    inner class ActivityHandler {
        fun clickRetry(view: View) {
            repositoriesViewModel.query.value = repositoriesViewModel.query.value
        }
    }

    inner class RepositoryHandler {
        fun clickItem(view: View, item: RepositoryItem) = openUrlInCustomTab(item.url)
    }

    inner class RepositoryAdapter : RecyclerView.Adapter<BindingHolder<ItemRepositoryBinding>>() {
        val itemHandler = RepositoryHandler()

        var items by Delegates.observable<List<RepositoryItem>>(emptyList()) { _, oldValue, newValue ->
            if (oldValue != newValue) notifyDataSetChanged()
        }

        init {
            setHasStableIds(true)
        }

        override fun getItemCount() = items.size

        override fun getItemId(position: Int) = position.toLong()

        override fun onBindViewHolder(holder: BindingHolder<ItemRepositoryBinding>, position: Int) {
            val binding = holder.binding
            binding.item = items[position]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            BindingHolder<ItemRepositoryBinding>(parent, R.layout.item_repository).apply {
                binding.itemHandler = itemHandler
            }
    }
}
