package pt.isec.ans.p03sketches

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.drawToBitmap
import pt.isec.ans.p03sketches.databinding.ActivityDrawingAreaBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class DrawingAreaActivity : AppCompatActivity() {
    lateinit var binding: ActivityDrawingAreaBinding
    lateinit var drawingArea: DrawingArea

    private var permissionsGranted : Boolean = false

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionsGranted = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val title = intent.getStringExtra("title") ?: getString(R.string.str_no_name)
        supportActionBar?.title = getString(R.string.sketches_title) + ": " + title

        //binding.frDrawingArea.setBackgroundColor(this.intent.getIntExtra(COLOR_KEY, Color.WHITE))

        val imagePath = intent.getStringExtra(IMAGE_KEY)

        if(imagePath == null){
            drawingArea = DrawingArea(this, intent.getIntExtra(COLOR_KEY, Color.WHITE))
        }else{
            drawingArea = DrawingArea(this, imagePath)
        }

        binding.frDrawingArea.addView(drawingArea)
        checkPermissions()
    }

    fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { return }

        val permission =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
            else android.Manifest.permission.WRITE_EXTERNAL_STORAGE

        requestPermissionLauncher.launch(permission)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100) {
            permissionsGranted = grantResults
                .all { it == PackageManager.PERMISSION_GRANTED }
            return
        }
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_drawing,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == R.id.mnSave -> {
                //DÚVIDA: É necessário fazer um intent ou algo parecido? Ou apenas é preciso fazer isto?
                item.isChecked = true
                var bitmap = drawingArea.drawToBitmap()
                BitmapToFile(bitmap, "test")
            }
            item.groupId == R.id.grpColors -> {
                item.isChecked = true
                drawingArea.lineColor = when(item.itemId){
                    R.id.mnWhite -> Color.WHITE
                    R.id.mnBlue -> Color.BLUE
                    R.id.mnYellow -> Color.YELLOW
                    else -> Color.BLACK
                }
            }
        }
        return true
    }

    private fun BitmapToFile(bitmap: Bitmap, fileNameToSave : String) : File? {
        var file: File? = null

//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos)
        try {
            //DÚVIDA: Para guardar na pasta Pictures só preciso de adicionar "/Pictures" à string que está a ser montada como parametro?
            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + fileNameToSave)
//            file = File("/Pictures")
            file.createNewFile() //DÚVIDA: Existe aqui uma exceção "Operation not permitted". É necessário pedir permissões além de
                                 //WRITE_EXTERNAL_STORAGE e MANAGE_EXTERNAL_STORAGE? Além disso as permissões não estão a ser pedidas
                                 //por algum motivo, há algo que esteja a faltar?

            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
            val bitmapdata = bos.toByteArray()

            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return file
        }

    }

    companion object{
        const val TITLE_KEY = "title"
        const val COLOR_KEY = "color"
        const val IMAGE_KEY = "image"

        fun getIntent(context: Context, title: String, color: Int):Intent{
            val intent = Intent(context, DrawingAreaActivity::class.java)
            intent.putExtra(TITLE_KEY, title)
            intent.putExtra(COLOR_KEY, color)
            return intent
        }

        fun getIntent(context: Context, title: String, imagePath : String?):Intent{
            val intent = Intent(context, DrawingAreaActivity::class.java)
            intent.putExtra(TITLE_KEY, title)
            intent.putExtra(IMAGE_KEY, imagePath)
            return intent
        }
    }
}