package com.secdev.mviapp.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.jakewharton.rxbinding2.view.RxView
import com.secdev.mviapp.R
import com.secdev.mviapp.model.MainView
import com.secdev.mviapp.utils.DataSource
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*

class MainActivity : MainView, MviActivity<MainView, MainPresenter>() {

    private lateinit var imageList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageList = createImageList()
    }

    private fun createImageList(): List<String> {
        return mutableListOf(
            "https://miro.medium.com/max/1400/0*XBDnu9eH32-jT3t-",
            "https://miro.medium.com/max/1400/0*p15b1gX94xWADMme",
            "https://miro.medium.com/max/1400/0*sOkKuGVdAKoOcuj7"
        )
    }

    override val imageIntent: Observable<Int>
        get() = RxView.clicks(btnMain)
            .map { _ -> getRandomNumberInRange(0, imageList.size - 1) }

    private fun getRandomNumberInRange(min: Int, max: Int): Int? {
        if (min >= max)
            throw IllegalArgumentException("Max must greater than min")

        val random = Random()
        return random.nextInt(max - min + 1) + min
    }

    override fun render(viewState: MainViewState) {
        if (viewState.isLoading) {
            progressMain.visibility = View.VISIBLE
            imageMain.visibility = View.GONE
            btnMain.isEnabled = false
        } else if (viewState.isImageViewShow) {
            Picasso
                .get()
                .load(viewState.imageLink)
                .fetch(object : Callback {
                    override fun onSuccess() {
                        progressMain.visibility = View.GONE
                        imageMain.visibility = View.VISIBLE
                        btnMain.isEnabled = true

                        imageMain.alpha = 0f
                        Picasso
                            .get()
                            .load(viewState.imageLink)
                            .into(imageMain)
                        imageMain.animate().setDuration(300).alpha(1f).start()
                    }

                    override fun onError(e: Exception?) {
                        Toast.makeText(
                            this@MainActivity,
                            e!!.message.toString(),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                })

        } else if (viewState.error != null) {
            progressMain.visibility = View.GONE
            imageMain.visibility = View.GONE
            btnMain.isEnabled = true
            Toast.makeText(
                this@MainActivity,
                viewState.error!!.message.toString(),
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    override fun createPresenter(): MainPresenter {
        return MainPresenter(DataSource(imageList))
    }

}
