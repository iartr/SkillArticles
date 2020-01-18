package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.widget.Checkable
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.bottombar
import kotlinx.android.synthetic.main.activity_root.submenu
import kotlinx.android.synthetic.main.coordinator.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.init

class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.coordinator)
        setupFabs()
        setupBottombar()
    }

    // Значения, куда должны будут сместиться фабы при открытии
    private fun setupFabs() {
        mini1.init(-96f, -96f)
        mini2.init(0f, -128f)
        mini3.init(96f, -96f)

        fab.setOnClickListener {
            if (mini1.isOrWillBeShown) mini1.hide() else mini1.show()
            if (mini2.isOrWillBeShown) mini2.hide() else mini2.show()
            if (mini3.isOrWillBeShown) mini3.hide() else mini3.show()
        }
    }

    private fun setupBottombar() {
        btn_like.setOnClickListener { view ->
            view as Checkable
            view.toggle()
            Snackbar.make(view, if (view.isChecked) "set like" else "unset like", Snackbar.LENGTH_LONG)
                .setAnchorView(bottombar)
                .show()
        }

        btn_settings.setOnClickListener { view ->
            view as Checkable
            view.toggle()
            if (view.isChecked) submenu.open() else submenu.close()
        }
    }

}
