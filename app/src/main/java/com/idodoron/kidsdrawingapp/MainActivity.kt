package com.idodoron.kidsdrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ib_brush_size.setOnClickListener{
            showBrushSizeDialog()
        }
    }

    private fun showBrushSizeDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size")
        brushDialog.sb_brush_size.progress = drawing_view.mBrushSize.roundToInt()
        brushDialog.tv_brush_size.text = brushDialog.sb_brush_size.progress.toString()
        brushDialog.show()

        brushDialog.sb_brush_size.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brushDialog.tv_brush_size.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar != null)
                    drawing_view.setSizeForBrush(seekBar.progress)
            }

        })

        val closeBtn = brushDialog.btn_close_brush_size_dialog
        closeBtn.setOnClickListener{
            brushDialog.dismiss()
        }
    }
}