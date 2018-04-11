package com.dalen.mhnettydemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.munin.mhnettyclientdemo.HeartCmd
import com.munin.mhnettyclientdemo.NettyClient
import com.munin.mhnettyclientdemo.R
import java.net.InetSocketAddress

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NettyClient.getInstance().connect(InetSocketAddress("192.168.1.120",8080))
    }
    fun click(view: View){
        NettyClient.getInstance().send(HeartCmd.heart())
    }
    fun cancel(view: View){
        NettyClient.getInstance().cancel()
    }
    fun reconnect(view: View){
        NettyClient.getInstance().connect()
    }
}
