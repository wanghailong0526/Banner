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
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.whl.banner.CircleIndicator
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    private var data = ArrayList<String>()
    private lateinit var indicatorView: CircleIndicator
    private lateinit var banner: Banner
    private val options = RequestOptions().transform(FitCenter(), RoundedCorners(10)).dontAnimate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        banner = findViewById<Banner>(R.id.banner)
        indicatorView = findViewById<CircleIndicator>(R.id.indicator)

        banner.setIndicatorView(indicatorView).addLifecycleObserver(this)
            .setAdapter(BannerAdapter(data)).setOnPageClickListener {
                Toast.makeText(this, "点击了 ${it + 1}", Toast.LENGTH_SHORT).show()
            }.setOnPageChangedListener { leftPageIndex, currentPagIndex, rightPageIndex ->

            }

        //请求数据
        banner.postDelayed(Runnable {
            data.add("https://i.hd-r.cn/68bb2fe2b8ef3cdf03d0ae0a64d12c72.jpg")
            data.add("https://i.hd-r.cn/415858a4318df361f773281eee89d462.jpg")
            data.add("https://i.hd-r.cn/e286e283694273fd3c41b989d4fe75c7.jpg")
            data.add("https://i.hd-r.cn/c4e86ea6b008a7b6c38fe5f78a102e15.jpg")
            data.add("https://i.hd-r.cn/9f0b37ddae6cc7e7fb32e8edd6f5cef3.jpg")
            data.add("https://i.hd-r.cn/3ba96485348e7c6989e3013d60e02e79.jpg")
            data.add("https://i.hd-r.cn/db074f74eeff4c7669fecc69b1f29147.jpg")
            data.add("https://i.hd-r.cn/31e46999c8c21e90737cee6207ccf42e.jpg")
            data.add("https://i.hd-r.cn/b6f395d6d9699fad9c29308b8ae6a985.jpg")
            data.add("https://i.hd-r.cn/f0b825618d8dcb156aaa57039b3ad52d.jpg")
            data.forEach() { url ->
                Glide.with(this.applicationContext).load(url).apply(options)
                    .preload(DensityUtil.dpToPx(177f), DensityUtil.dpToPx(112f))
            }

            /**
             * 数据返回后更新
             */
            banner.notifiedDataSetChanged()
        }, 1000)
    }
}


class BannerAdapter constructor(private var dataSet: ArrayList<String>) :
    Banner.Adapter<BannerViewHolder>() {
    val options: RequestOptions =
        RequestOptions().transform(FitCenter(), RoundedCorners(10)).dontAnimate()


    override fun onCreateViewHolder(parent: ViewGroup): BannerViewHolder {
        return BannerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.banner_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
//        holder.iv.setImageResource(dataSet.get(position))
        Glide.with(holder.itemView.context).load(dataSet.get(position)).apply(options)
            .into(holder.iv);
    }

    override fun getItemCount(): Int {
        return if (dataSet != null) dataSet.size else 0
    }
}

class BannerViewHolder(itemView: View) : Banner.ViewHolder(itemView) {
    val iv = itemView.findViewById<ImageView>(R.id.iv)
}
