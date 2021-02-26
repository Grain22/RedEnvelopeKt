package grain.red.redenvelopekt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.constraintlayout.solver.widgets.ConstraintHorizontalLayout
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Constraints
import androidx.core.view.updateLayoutParams
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        findViewById<Button>(R.id.openService).setOnClickListener {
            Toast.makeText(
                applicationContext,
                "go to service",
                Toast.LENGTH_LONG
            ).show()
            GlobalScope.launch {
                delay(500L)
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        findViewById<Button>(R.id.update_button_position).setOnClickListener {
            val x: Float =
                findViewById<EditText>(R.id.position_left).text?.toString()?.toFloat() ?: 0.5F
            val y: Float =
                findViewById<EditText>(R.id.position_top).text?.toString()?.toFloat() ?: 0.6F
            Toast.makeText(
                applicationContext,
                "new position set to x: $x y: $y",
                Toast.LENGTH_SHORT
            ).show()
            findViewById<Button>(R.id.analog_button).translationX = x
            findViewById<Button>(R.id.analog_button).translationY = y
            val c = ConstraintSet()
            c.clone(this.applicationContext,R.layout.activity_main)
            c.setHorizontalBias(R.id.analog_button,0F)
            c.setVerticalBias(R.id.analog_button,0F)
        }

        findViewById<Button>(R.id.update_sleep).setOnClickListener {
            Toast.makeText(
                applicationContext,
                "test click ${resources.displayMetrics.densityDpi}",
                Toast.LENGTH_SHORT
            ).show()
        }
        findViewById<Button>(R.id.analog_button).setOnClickListener {
            Toast.makeText(
                applicationContext,
                "ohh.......................",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
