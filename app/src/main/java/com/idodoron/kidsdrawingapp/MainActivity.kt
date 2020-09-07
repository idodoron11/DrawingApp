package com.idodoron.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorChangedListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ib_brush_size.setOnClickListener{
            showBrushSizeDialog()
        }

        ib_color_picker.setOnClickListener{
            showColorPickerDialog()
        }

        ib_import_background.setOnClickListener {
            if(isPermissionAllowed(Manifest.permission.READ_EXTERNAL_STORAGE)){
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, GALLERY)
            } else {
                requestStoragePermission()
            }
        }

        ib_undo.setOnClickListener {
            drawing_view.undoLastChange()
        }

        ib_redo.setOnClickListener {
            drawing_view.redoLastChange()
        }

        ib_save.setOnClickListener {
            if(isPermissionAllowed(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                BitmapAsyncTask(getBitmapFromView(fl_drawing_view_container)).execute()
            } else {
                requestStoragePermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == GALLERY){
            try{
                if(data!!.data != null){
                    iv_background.visibility = View.VISIBLE
                    iv_background.setImageURI(data.data)
                } else {
                    Toast.makeText(this@MainActivity,
                        "There something wrong with the image you've chosen.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch(e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun showBrushSizeDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size")
        brushDialog.sb_brush_size.progress = ((drawing_view.mBrushSize.toFloat() / (drawing_view.maxBrushSize - drawing_view.minBrushSize))*100).roundToInt()
        brushDialog.tv_brush_size.text = drawing_view.mBrushSize.toString()
        brushDialog.show()

        brushDialog.sb_brush_size.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size =
                    (progress / 100f) * (drawing_view.maxBrushSize - drawing_view.minBrushSize)
                val sizeStr = String.format("%.2f", size)
                brushDialog.tv_brush_size.text = sizeStr
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null)
                    drawing_view.setSizeForBrush(seekBar.progress)
            }

        })

        val closeBtn = brushDialog.btn_close_brush_size_dialog
        closeBtn.setOnClickListener{
            brushDialog.dismiss()
        }
    }

    private fun showColorPickerDialog(){
        val colorPickerDialog = ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose color")
            .initialColor(drawing_view.color)
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(12)
            .setPositiveButton(
                "ok"
            ) { dialog, selectedColor, allColors -> drawing_view.color = selectedColor }
            .setNegativeButton(
                "cancel"
            ) { dialog, which -> /* don't do anything */}
            .build()
            .show()
    }

    private fun requestStoragePermission(){
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions.toString())){
            Toast.makeText(this,
                "Need permission to add a background.",
                Toast.LENGTH_SHORT)
                .show()
        }
        ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity,
                    "Permission was granted.",
                    Toast.LENGTH_SHORT)
                    .show()
            }
            else{
                Toast.makeText(this@MainActivity,
                    "Permission was denied.",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun isPermissionAllowed(perm: String): Boolean{
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private inner class BitmapAsyncTask(val mBitmap: Bitmap):
        AsyncTask<Any, Void, String>(){

        private lateinit var mProgressDialog: Dialog

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result = ""
            if(mBitmap != null){
                try{
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val filepath: String = (externalMediaDirs[0].absoluteFile.toString()
                            + File.separator
                            + "KidDrawingApp_"
                            + System.currentTimeMillis() / 1000
                            + ".png")
                    val f = File(filepath)
                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result = f.absolutePath
                } catch(e: java.lang.Exception){
                    e.printStackTrace()
                    result =  ""
                }
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if(result!!.isNotEmpty()){
                Toast.makeText(this@MainActivity,
                    "File saved in:\n$result",
                    Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this@MainActivity,
                    "File save error.",
                    Toast.LENGTH_SHORT)
                    .show()
            }

            MediaScannerConnection.scanFile(
                this@MainActivity, arrayOf(result), null
            ) { path, uri ->
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/png"
                }
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share image to..."
                    )
                )
            }
        }

        private fun showProgressDialog(){
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.dialog_please_wait)
            mProgressDialog.show()
        }

        private fun cancelProgressDialog(){
            mProgressDialog.dismiss()
        }

    }

    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}