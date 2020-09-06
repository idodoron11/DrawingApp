package com.idodoron.kidsdrawingapp

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorChangedListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
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

        ib_color_picker.setOnClickListener{
            showColorPickerDialog()
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
}