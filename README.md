# GifApngPlayer


-----


### 功能：Features:

    1. 支持 GIF/APNG 无缝切换。Seamless switching animation GIF/APNG.

    2. 支持 Android/Java Swing。Support Android/Java Swing.

    3. 支持暂停和继续动画。Support Pause/Resume animation.


-----


## Android使用：For Android:

![](https://github.com/NeedJava1980/GifApngPlayer/raw/master/snapshot/AnimatedPlayer2.gif)


### 如何开始：How to start:

    AnimateManager mManager = new AnimateManager();

    mManager.setSurfaceBackground( mSurfaceBackground );  //Optional, for example Color.GRAY

    mManager.setView( mSurfaceView );  //android.view.SurfaceView or android.widget.ImageView

    mManager.setUri( intent.getData() );  //MUST after setView

    mManager.restart( mManager.getInputStream() );


### 更改 GIF/APNG 文件（有文件后缀）：Change GIF/APNG file (Has file extension):

    mManager.setTerminated();

    mManager.setUri( intent.getData() );  //MUST after setView, sample.gif or sample.png

    mManager.restart( mManager.getInputStream() );


### 更改 GIF/APNG 文件（无文件后缀）：Change GIF/APNG file (No file extension):

    mManager.setTerminated();

    mManager.setUri( intent.getData() );  //MUST after setView, sample.gif or sample.png

    mManager.setType( AnimateManager.TYPE_GIF );  //AnimateManager.TYPE_GIF or AnimateManager.TYPE_APNG

    mManager.restart( mManager.getInputStream() );


### 更改 GIF/APNG 渲染画布：Change GIF/APNG rendering view:

    mManager.setTerminated();

    mManager.setSurfaceBackground( mSurfaceBackground );  //Optional, for example Color.GRAY

    mManager.setView( mSurfaceView );  //android.view.SurfaceView or android.widget.ImageView

    mManager.restart( mManager.getInputStream() );


### 暂停 或 继续：Pause or Resume:

    mManager.setPaused( ! mManager.isPaused() );


### 不要忘记：DO NOT forget:

    mManager.setTerminated();


-----


## Swing使用：For Java Swing:

![](https://github.com/NeedJava1980/GifApngPlayer/raw/master/snapshot/AnimatedPlayer1.gif)


### 如何开始：How to start:

    AnimateManager mManager = new AnimateManager();

    mManager.setFile( new java.io.File( "sample.gif" ) );

    mManager.setView( mLabel );  //javax.swing.JLabel

    mManager.restart( mManager.getInputStream() );


### 更改 GIF/APNG 文件（有文件后缀）：Change GIF/APNG file (Has file extension):

    mManager.setTerminated();

    mManager.setFile( new java.io.File( "sample.gif" ) );  //sample.gif or sample.png

    mManager.restart( mManager.getInputStream() );


### 更改 GIF/APNG 文件（无文件后缀）：Change GIF/APNG file (No file extension):

    mManager.setTerminated();

    mManager.setFile( new java.io.File( "sample" ) );

    mManager.setType( AnimateManager.TYPE_GIF );  //AnimateManager.TYPE_GIF or AnimateManager.TYPE_APNG

    mManager.restart( mManager.getInputStream() );


### 更改 GIF/APNG 渲染画布：Change GIF/APNG rendering view:

    mManager.setTerminated();

    mManager.setView( mLabel );  //javax.swing.JLabel

    mManager.restart( mManager.getInputStream() );


### 暂停 或 继续：Pause or Resume:

    mManager.setPaused( ! mManager.isPaused() );


### 注意不要忘记：DO NOT forget:

    mManager.setTerminated();


-----


### 捐赠：Donate:

[PayPal](https://paypal.me/needjava?locale.x=zh_XC)

![Alipay](https://github.com/NeedJava1980/GifApngPlayer/raw/master/snapshot/alipay.png)
