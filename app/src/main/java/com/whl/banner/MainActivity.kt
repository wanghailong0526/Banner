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
import com.whl.banner.CircleIndicator

class MainActivity : ComponentActivity() {

    private var data = ArrayList<Int>()
    private lateinit var indicatorView: CircleIndicator
    private lateinit var banner: Banner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var width = DensityUtil.pxToDp(this, 1280f);
        var height = DensityUtil.pxToDp(this, 650f);
        var aa = DensityUtil.pxToDp(this, 350f);
        var bb = DensityUtil.pxToDp(this, 170f);
        println("whl ***" + "宽：" + width + " 高：" + height + " aa:" + aa + " bb:" + bb)

        banner = findViewById<Banner>(R.id.banner)
        indicatorView = findViewById<CircleIndicator>(R.id.indicator)

        banner.setIndicatorView(indicatorView).addLifecycleObserver(this)
            .setAdapter(BannerAdapter(data)).setOnPageClickListener {
                Toast.makeText(this, "点击了 ${it + 1}", Toast.LENGTH_SHORT).show()
            }.setOnPageChangedListener { leftPageIndex, currentPagIndex, rightPageIndex ->

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
        return if (dataSet != null) dataSet.size else 0
    }
}

class BannerViewHolder(itemView: View) : Banner.ViewHolder(itemView) {
    val iv = itemView.findViewById<ImageView>(R.id.iv)
}
