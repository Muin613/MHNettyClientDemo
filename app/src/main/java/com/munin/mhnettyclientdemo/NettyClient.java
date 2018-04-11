package com.munin.mhnettyclientdemo;

import android.util.Log;
import java.net.InetSocketAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;


public class NettyClient {
    private static final String TAG = "NettyClient";
    private InetSocketAddress mServerAddress;
    private Bootstrap mBootstrap;
    private Channel mChannel;
    private EventLoopGroup mWorkerGroup;
    private static NettyClient INSTANCE;

    private NettyClient() {
    }

    public static NettyClient getInstance() {
        if (INSTANCE == null) {
            synchronized (NettyClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NettyClient();
                }
            }
        }
        return INSTANCE;
    }

    public void connect(final InetSocketAddress socketAddress) {
        if (mChannel != null && mChannel.isActive()) {
            return;
        }
        mServerAddress = socketAddress;

        if (mBootstrap == null) {
            mWorkerGroup = new NioEventLoopGroup();
            mBootstrap = new Bootstrap();
            mBootstrap.group(mWorkerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("decoder", new ByteArrayDecoder());
                            pipeline.addLast("encoder", new ByteArrayEncoder());
                            pipeline.addLast("handler", new SimpleChannelInboundHandler<byte[]>() {
                                @Override
                                protected void messageReceived(ChannelHandlerContext ctx, byte[] msg) throws Exception {
                                    Log.i(TAG, "接收" + msg);
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    Log.i(TAG, "接收1" + (msg instanceof byte[]));
                                    super.channelRead(ctx, msg);
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    Log.i(TAG, "正在连接");
                                    super.channelActive(ctx);
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    Log.i(TAG, "连接关闭");
                                    super.channelInactive(ctx);
                                }
                            });

                        }
                    })
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
        }

        ChannelFuture future = mBootstrap.connect(mServerAddress);
        future.addListener(mConnectFutureListener);
    }

    private ChannelFutureListener mConnectFutureListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture pChannelFuture) throws Exception {
            if (pChannelFuture.isSuccess()) {
                mChannel = pChannelFuture.channel();
                Log.i(TAG, "连接!");
            } else {

                Log.i(TAG, "连接失败!");
            }
        }

    };

    public synchronized void send(byte[] msg) {
        if (mChannel == null) {
            Log.e(TAG, "send: channel is null");
            return;
        }
        if (!mChannel.isWritable()) {
            Log.e(TAG, "send: channel is not Writable");
            return;
        }
        if (!mChannel.isActive()) {
            Log.e(TAG, "send: channel is not active!");
            return;
        }
        Log.e(TAG, "发送" + msg);
        if (mChannel != null) {
            mChannel.writeAndFlush(msg);
        }
    }

    public synchronized void cancel() {
        if (null != mWorkerGroup)
            mWorkerGroup.shutdownGracefully();
        mWorkerGroup = null;
        mBootstrap = null;
    }

    public void connect() {
        if (null != mServerAddress)
            connect(mServerAddress);
    }
}
