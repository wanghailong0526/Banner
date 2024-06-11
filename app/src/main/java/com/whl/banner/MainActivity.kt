package com.whl.banner

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.whl.banner.indicator.CircleIndicatorView

class MainActivity : ComponentActivity() {

    private var data = ArrayList<Int>()
    private lateinit var indicatorView: CircleIndicatorView
    private lateinit var banner: Banner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        banner = findViewById<Banner>(R.id.banner)
        indicatorView = CircleIndicatorView(banner.context)

        banner.setIndicatorView(indicatorView)
            .setIndicatorViewTopSpace(BannerUtils.dp2px(20f))
            .addLifecycleObserver(this)
            .setScaleValue(0.3f)
            .setAdapter(BannerAdapter(data))
            .setOnPageClickListener {
                Toast.makeText(this, "点击了 ${it + 1}", Toast.LENGTH_SHORT).show()
            }
            .setOnPageChangedListener { leftPageIndex, currentPagIndex, rightPageIndex ->

            }

        //请求数据
        banner.postDelayed(Runnable {
            data.add(R.drawable.image1)
            data.add(R.drawable.image2)
            data.add(R.drawable.image3)
            data.add(R.drawable.image4)
            data.add(R.drawable.image5)
            data.add(R.drawable.image6)
            data.add(R.drawable.image7)
            data.add(R.drawable.image8)
            data.add(R.drawable.image9)
            data.add(R.drawable.image10)

            /**
             * 数据返回后更新
             */
            banner.notifiedDataSetChanged()
        }, 2000)
    }

    override fun onResume() {
        super.onResume()
        println("whl *** MainActivity onResume")

    }
}


class BannerAdapter(private var dataSet: ArrayList<Int>) : Banner.Adapter<BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): BannerViewHolder {
        return BannerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.banner_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.iv.setImageResource(dataSet.get(position))
    }

    override fun getItemCount(): Int {
        return dataSet?.size ?: 0
    }
}

class BannerViewHolder(itemView: View) : Banner.ViewHolder(itemView) {
    val iv = itemView.findViewById<ImageView>(R.id.iv)
}
