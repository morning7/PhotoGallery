package com.karson.photogallery

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.karson.network.ThumbnailDownloader
import com.karson.network.api.GalleryItem
import com.karson.photogallery.databinding.FragmentPhotoBinding
import com.karson.photogallery.databinding.ListItemGalleryBinding
import com.karson.utils.PREF_IS_POLLING
import com.karson.utils.QueryPreferences
import com.karson.work.POLL_WORK
import com.karson.work.PollWorker
import java.util.concurrent.TimeUnit

private const val SPAN_COUNT = 3
private const val TAG = "PhotoFragment"

class PhotoGalleryFragment : VisibleFragment(){
    private lateinit var binding: FragmentPhotoBinding
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>


    private val viewModel by lazy {
        ViewModelProvider(this)[PhotoGalleryViewModel::class.java]
    }

    companion object {
        val instance: PhotoGalleryFragment by lazy {
            PhotoGalleryFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
        val responseHandler = Handler()
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

//        val constraints = Constraints.Builder()
//            //非流量網絡
//            .setRequiredNetworkType(NetworkType.UNMETERED)
//            .build()
//        val workRequest = OneTimeWorkRequest.Builder(PollWorker::class.java)
//            .setConstraints(constraints)
//            .build()
//        WorkManager.getInstance().enqueue(workRequest)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoBinding.inflate(inflater, container, false)
        binding.rvPhoto.layoutManager = GridLayoutManager(requireActivity(), SPAN_COUNT)
        lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.galleryItemLiveData.observe(viewLifecycleOwner, {
            Log.d(TAG, it.toString())
            binding.rvPhoto.adapter = PhotoAdapter(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)
        val searchItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.d(TAG, "onQueryTextSubmit : $query")
                    viewModel.searchPhotos(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d(TAG, "onQueryTextChange : $newText")
                    return false
                }

            })
        }

        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.getBooleanValue(requireContext(), PREF_IS_POLLING)
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_item_clear -> {
                viewModel.searchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.getBooleanValue(requireContext(), PREF_IS_POLLING)
                if (isPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreferences.putBooleanValue(requireContext(), PREF_IS_POLLING, false)
                } else {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest.Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(POLL_WORK, ExistingPeriodicWorkPolicy.KEEP, periodicRequest)
                    QueryPreferences.putBooleanValue(requireContext(), PREF_IS_POLLING, true)
                }
                requireActivity().invalidateOptionsMenu()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    private inner class PhotoHolder(private val binding: ListItemGalleryBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private lateinit var item: GalleryItem

        fun bindDrawable(drawable: Drawable) {
            binding.ivCover.setImageDrawable(drawable)
        }

//        val bindDrawable: (Drawable) -> Unit = binding.ivCover::setImageDrawable

        fun bind(item: GalleryItem, position: Int) {
            this.item = item
            binding.tvName.text = position.toString()
        }

        override fun onClick(v: View) {
//            val intent = Intent(Intent.ACTION_VIEW, item.photoPageUri)
//            startActivity(intent)
//            val intent = PhotoPageActivity.newIntent(requireContext(), item.photoPageUri)
//            startActivity(intent)

            CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.black))
                .setShowTitle(true)
                .build()
                .launchUrl(requireContext(), Uri.parse("https://www.baidu.com"))
        }
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) : RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val binding = ListItemGalleryBinding.inflate(layoutInflater, parent, false)
            return PhotoHolder(binding)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
//            ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)?.also {
//                holder.bindDrawable(it)
//            }
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
            holder.bind(galleryItem, position)
        }

        override fun getItemCount() = galleryItems.size
    }
}