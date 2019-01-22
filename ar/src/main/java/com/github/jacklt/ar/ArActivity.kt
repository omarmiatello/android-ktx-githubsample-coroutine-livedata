package com.github.jacklt.ar

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.jacklt.ar.databinding.ArviewRepositoryBinding
import com.github.jacklt.githubsample.OrganizationRepositoriesViewModel
import com.github.jacklt.githubsample.data.RepositoryItem
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class ArActivity : AppCompatActivity() {

    val repos by lazy { intent.getSerializableExtra("r") as List<RepositoryItem>? ?: emptyList() }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkIsSupportedDeviceOrFinish()

        setContentView(R.layout.activity_ar)
        val arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment



        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->

            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val baseNode = Node()
            baseNode.setParent(anchorNode)


            repos.forEachIndexed { index, repositoryItem ->

                // Create the transformable andy and add it to the anchor.
                TransformableNode(arFragment.transformationSystem).apply {
                    setParent(baseNode)

                    ArviewRepositoryBinding.inflate(layoutInflater).apply { item = repositoryItem }.root
                        .toViewRenderable().thenAccept { renderable = it }
                    localPosition = Vector3(0f, 0.8f, - index * 1f)
                }
            }


        }
    }

    @RequiresApi(VERSION_CODES.N)
    private fun View.toViewRenderable(builder: (ViewRenderable.Builder) -> Unit = {}) =
        ViewRenderable.builder().setView(this@ArActivity, this).also(builder).build()

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * Finishes the activity if Sceneform can not run
     */
    @SuppressLint("NewApi")
    fun checkIsSupportedDeviceOrFinish(): Boolean {
        when {
            Build.VERSION.SDK_INT < VERSION_CODES.N -> {
                Log.e(TAG, "Sceneform requires Android N or later")
                Toast.makeText(this, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
                finish()
                return false
            }
            getSystemService(ActivityManager::class.java)
                .deviceConfigurationInfo
                .glEsVersion
                .toDouble() < 3.0 -> {
                Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
                Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show()
                finish()
                return false
            }
            else -> return true
        }
    }

    companion object {
        val TAG = "AR"
    }

}
