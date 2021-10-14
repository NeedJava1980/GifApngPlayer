package com.needjava.animateplayer;

import java.io.InputStream;

import android.net.Uri;

import android.view.View;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import android.widget.ImageView;

import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;

import com.needjava.animate.Decoder;
import com.needjava.animate.DecoderGif;
import com.needjava.animate.DecoderApng;
import com.needjava.animate.AnimateFrame;
import com.needjava.animate.AnimateReader;
import com.needjava.animate.AnimateBalancer;
import com.needjava.animate.AnimateRenderer;
import com.needjava.animate.AnimateReadListener;
import com.needjava.animate.AnimateRendererListener;

/**
 * @author NeedJava1980@gmail.com 08/16/2017
 */
public final class AnimateManager implements SurfaceHolder.Callback
{
    public static final int TYPE_UNKNOWN  = 0;

    public static final int TYPE_GIF      = 1;

    public static final int TYPE_APNG     = 2;

    private AnimateReadListener mListener;

    private Uri mUri;

    private int mType;

    private View mView;

    private boolean mIsReduceSize;

    private boolean mIsSeekable;

    private AnimateReader mReader;

    private AnimateRenderer mRenderer;

    private int mSurfaceBackground;

    private SurfaceHolder mHolder;

    private float mSurfaceWidth;

    private float mSurfaceHeight;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override public final void surfaceCreated( final SurfaceHolder holder ){}

    @Override public final void surfaceDestroyed( final SurfaceHolder holder ){}

    @Override public final void surfaceChanged( final SurfaceHolder holder, final int format, final int width, final int height )
    {
        if( width < 1 || height < 1 ){ return; }

        mSurfaceWidth = width;

        mSurfaceHeight = height;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final void setListener( final AnimateReadListener listener )
    {
        mListener = listener;
    }

    public final void setUri( final Uri uri )
    {
        if( mView == null ){ throw new IllegalArgumentException( "Call setView firstly." ); }

        mUri = uri;

        setType( getTypeFromMime( mUri ) );

        //content://com.android.providers.media.documents/document/image%3A844235

        //content://com.android.externalstorage.documents/document/0766-0CBB%3ADCIM%2Felephant_apng_zopfli.png

        //content://media/external/images/media/844235

        //System.err.println( "[setUri] mUri: " + mUri + "  mType: " + mType );
    }

    public final void setType( final int type )  //NOTE:If no file extension or MIME, you have to call this
    {
        mType = type;
    }

    public final void setView( final View view )
    {
        mView = view;
    }

    public final void setSurfaceBackground( final int background )  //NOTE:You have to provide background color if you want to use SurfaceView
    {
        mSurfaceBackground = background;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final boolean isReduceSize()
    {
        return mIsReduceSize;
    }

    public final void setReduceSize( final boolean isReduceSize )
    {
        mIsReduceSize = isReduceSize;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final boolean isSeekable()
    {
        return mIsSeekable;
    }

    public final void setSeekable( final boolean isSeekable )
    {
        mIsSeekable = isSeekable;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final boolean isPaused()
    {
        return ( mRenderer == null ? false : mRenderer.isPaused() );  //Can not use Decoder's isPaused
    }

    public final void setPaused( final boolean paused )
    {
        if( mReader != null ){ mReader.setPaused( paused ); }  //Step 1. Pause reader

        if( mRenderer != null ){ mRenderer.setPaused( paused ); }  //Step 1. Pause mRenderer
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final boolean isTerminated()
    {
        return ( mRenderer == null ? false : mRenderer.isTerminated() );  //Can not use Decoder's isTerminated
    }

    public final void setTerminated()
    {
        if( mReader != null ){ mReader.setTerminated(); }  //Step 1. Terminate reader

        if( mRenderer != null ){ mRenderer.setTerminated(); }  //Step 2. Terminate renderer

        if( mHolder != null ){ mHolder.removeCallback( AnimateManager.this ); }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final void restart( final InputStream input )
    {
        setTerminated();  //Must call this to avoid OOM

        if( mView instanceof SurfaceView )
        {
            final SurfaceView surfaceView = (SurfaceView)mView;

            mHolder = surfaceView.getHolder();

            mHolder.addCallback( AnimateManager.this );

            mHolder.setFormat( PixelFormat.RGBA_8888 );  //Remove black background

            mRenderer = new AndroidAnimateSurfaceViewRenderer( new AndroidAnimateRendererListener() ); mRenderer.start();  //Step3. New renderer, start and waiting for decoding

            final AnimateBalancer balancer = new AnimateBalancer( mRenderer, mIsReduceSize );

            final Decoder decoder = ( mType == TYPE_GIF ? new DecoderGif( balancer ) : ( mType == TYPE_APNG ? new DecoderApng( balancer ) : null ) ); if( decoder == null ){ return; }

            mReader = new AnimateReader( mListener, input, decoder ); mReader.start();  //Step4. New reader, start and begin decoding
        }
        else if( mView instanceof ImageView )
        {
            final ImageView imageView = (ImageView)mView;

            mRenderer = new AndroidAnimateImageViewRenderer( new AndroidAnimateRendererListener(), imageView ); mRenderer.start();  //Step3. New renderer, start and waiting for decoding

            final AnimateBalancer balancer = new AnimateBalancer( mRenderer, mIsReduceSize );

            final Decoder decoder = ( mType == TYPE_GIF ? new DecoderGif( balancer ) : ( mType == TYPE_APNG ? new DecoderApng( balancer ) : null ) ); if( decoder == null ){ return; }

            mReader = new AnimateReader( mListener, input, decoder ); mReader.start();  //Step4. New reader, start and begin decoding
        }
    }

    public final InputStream getInputStream()  //NOTE:Network should download GIF/APNG firstly, then try to open file from disk
    {
        try{ return mView.getContext().getContentResolver().openInputStream( mUri ); }

        catch( Exception e ){ e.printStackTrace(); return null; }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final int getTypeFromMime( final Uri uri )
    {
        try{ final String mime = mView.getContext().getContentResolver().getType( mUri ); return ( "image/gif".equals( mime ) ? TYPE_GIF : ( "image/png".equals( mime ) || "image/apng".equals( mime ) ) ? TYPE_APNG : TYPE_UNKNOWN ); }

        catch( Exception e ){ e.printStackTrace(); return TYPE_UNKNOWN; }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final class AndroidAnimateSurfaceViewRenderer extends AnimateRenderer
    {
        private final Paint mPaint;

        public AndroidAnimateSurfaceViewRenderer( final AnimateRendererListener listener )
        {
            super( listener, mIsSeekable, false/*HARDCODE*/ );

            this.mPaint = new Paint();
        }

        @Override public final boolean onNotifyRenderer( final AnimateFrame frame, final int frameCount, final int frameIndex, final boolean fromRenderer )
        {
            if( frame == null || mHolder == null ){ return false; }

            final Canvas canvas = mHolder.lockCanvas(); if( canvas == null ){ return false; }

            if( ( mSurfaceWidth * frame.mHeight ) > ( mSurfaceHeight * frame.mWidth ) )
            {
                final float scale = ( mSurfaceHeight / frame.mHeight );

                canvas.translate( ( mSurfaceWidth - frame.mWidth * scale ) / 2F, 0F );

                canvas.scale( scale, scale );
            }
            else
            {
                final float scale = ( mSurfaceWidth / frame.mWidth );

                canvas.translate( 0F, ( mSurfaceHeight - frame.mHeight * scale ) / 2F );

                canvas.scale( scale, scale );
            }

            canvas.drawColor( mSurfaceBackground );  //NOTE:You must provide background color for SurfaceView

            canvas.drawBitmap( frame.mColorPixels, 0, frame.mWidth, 0F, 0F, frame.mWidth, frame.mHeight, true, mPaint );

            mHolder.unlockCanvasAndPost( canvas );

            //try{ bitmap.compress( Bitmap.CompressFormat.PNG, 100, new java.io.FileOutputStream( "/sdcard/Pictures/" + frameIndex + ".png" ) ); }catch( Exception e ){}

            return true;
        }
    }

    private final class AndroidAnimateImageViewRenderer extends AnimateRenderer
    {
        private final ImageView mImageView;

        public AndroidAnimateImageViewRenderer( final AnimateRendererListener listener, final ImageView imageView )
        {
            super( listener, mIsSeekable, false/*HARDCODE*/ );

            this.mImageView = imageView;
        }

        @Override public final boolean onNotifyRenderer( final AnimateFrame frame, final int frameCount, final int frameIndex, final boolean fromRenderer )
        {
            if( mImageView == null || frame == null ){ return false; }

            final Bitmap bitmap = Bitmap.createBitmap( frame.mColorPixels, frame.mWidth, frame.mHeight, Bitmap.Config.ARGB_8888 ); if( bitmap == null ){ return false; }

            mImageView.postDelayed( new Runnable(){ public final void run(){ if( mImageView == null ){ return; } mImageView.setImageBitmap( bitmap ); } }, 0L );  //NOTE:Must run on UI thread

            //try{ bitmap.compress( Bitmap.CompressFormat.PNG, 100, new java.io.FileOutputStream( "/sdcard/Pictures/" + frameIndex + ".png" ) ); }catch( Exception e ){}

            return true;
        }
    }

    private final class AndroidAnimateRendererListener implements AnimateRendererListener
    {
        @Override public final void onRenderFinished( final boolean seekable, final boolean paused )
        {
            if( seekable ){ return; }  //It is not large GIF/APNG, just loop the frame list

            restart( getInputStream() );  //It is large GIF/APNG, decode the GIF/APNG file from the beginning
        }
    }
}