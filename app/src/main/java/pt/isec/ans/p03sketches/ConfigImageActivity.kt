package pt.isec.ans.p03sketches

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import pt.isec.ans.p03sketches.databinding.ActivityConfigImageBinding
import java.io.File

class ConfigImageActivity : AppCompatActivity() {
    lateinit var binding:ActivityConfigImageBinding
    private var mode : Int = GALLERY
    private var permissionsGranted : Boolean = false
        set(value) {
            field = value
            binding.btnImage.isEnabled = value
        }

    private var imagePath : String? = null
    private var fileUri : Uri? = null

    var startActivityForGalleryResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult() ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultIntent = result.data
            resultIntent?.data?.let { uri ->
                imagePath = createFileFromUri(this, uri)
                updatePreview()
            }
        }
    }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
            permissionsGranted = isGranted
    }

    var startActivityForTakePhotoResult = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            imagePath = null
        }
        updatePreview()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getIntExtra(TYPE_KEY, GALLERY)

        when(mode){
            GALLERY -> {
                binding.btnImage.text = getString(R.string.btn_choose_image)
                binding.btnImage.setOnClickListener{onGalleryPressed()}
            }
            PHOTO -> {
                binding.btnImage.text = getString(R.string.btn_take_photo)
                binding.btnImage.setOnClickListener{onPhotoPressed()}
            }
        }
        //checkPermissions_V1()
        //checkPermissions_V2()
        checkPermissions_V3()
        updatePreview()
    }

    fun checkPermissions_V1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    ),
                    PERMISSION_CODE
                )
            }
        }
    }

    fun checkPermissions_V2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionLauncher.launch(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    fun checkPermissions_V3() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { return }

        val permission =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
            else android.Manifest.permission.READ_EXTERNAL_STORAGE

        requestPermissionLauncher.launch(permission)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CODE) {
            permissionsGranted = grantResults
                .all { it == PackageManager.PERMISSION_GRANTED }
            return
        }
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, info: Intent?)
    {
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && info != null) {
            info.data?.let { // this : Uri
                val cursor = contentResolver.query(it,
                    arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
                if (cursor !=null && cursor.moveToFirst())
                    imagePath = cursor.getString(0)
                // update image view or similar
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, info)
    }

    private fun updatePreview() {
        if(imagePath == null) {
            binding.frPreview.background = ResourcesCompat.getDrawable(resources,
                R.drawable.ic_sketches_foreground,
                null)
        } else {
            setPic(binding.frPreview, imagePath!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mnCreate) {
            if (binding.edTitle.text.trim().isEmpty()) {
                Snackbar.make(binding.edTitle,
                    R.string.msg_empty_title, Snackbar.LENGTH_LONG)
                    .show()
                binding.edTitle.requestFocus()
                return true
            }

            if(imagePath == null){
                Snackbar.make(binding.edTitle,
                    getString(R.string.img_missing),
                    Snackbar.LENGTH_LONG)
                    .show()

                return true
            }

            val intent = Intent(DrawingAreaActivity.getIntent(
                this,
                binding.edTitle.text.trim().toString(),
                imagePath
            ))
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onPhotoPressed() {
        imagePath = getTempFilename(this)
        startActivityForTakePhotoResult.launch(
            FileProvider.getUriForFile(this,
                "pt.isec.amov.sketches.android.fileprovider",
                File(imagePath))
        )
    }

    private fun onGalleryPressed() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        startActivityForGalleryResult.launch(intent)
    }

    companion object{
        const val GALLERY = 1
        const val PHOTO = 2
        const val TYPE_KEY = "type"
        const val PERMISSION_CODE = 1234
        const val GALLERY_REQUEST_CODE = 10

        fun getGalleryIntent(context: Context) : Intent {
            val intent = Intent(context, ConfigImageActivity::class.java)
            intent.putExtra(TYPE_KEY, GALLERY)
            return intent
        }

        fun getPhotoIntent(context: Context) : Intent {
            val intent = Intent(context, ConfigImageActivity::class.java)
            intent.putExtra(TYPE_KEY, PHOTO)
            return intent
        }
    }
}