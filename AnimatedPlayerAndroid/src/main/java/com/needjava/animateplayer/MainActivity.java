package com.needjava.animateplayer;

import java.io.InputStream;

import android.Manifest;

import android.os.Bundle;

import android.app.Activity;

import android.net.Uri;

import android.view.View;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.CompoundButton;

import android.content.Intent;

import android.content.pm.PackageManager;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;

import com.needjava.animate.Decoder;
import com.needjava.animate.GifDecoder;
import com.needjava.animate.ApngDecoder;
import com.needjava.animate.AnimateFrame;
import com.needjava.animate.AnimateReader;
import com.needjava.animate.AnimateBalancer;
import com.needjava.animate.AnimateRenderer;
import com.needjava.animate.AnimateRendererListener;

/**
 * NOTE:Use release build, the release build has better performance than the debug build.
 *
 * @author NeedJava1980@gmail.com 08/16/2017
 *
 * @version 04/21/2019 APNG support
 */
public final class MainActivity extends Activity
{
    private static final int SELECT_ANIMATE   = 1314;

    private AnimateManager mManager;

    private SurfaceView mSurfaceView;

    private ImageView mImageView;

    private CheckBox mCheckReduce;

    private CheckBox mCheckSurface;

    private Button mButtonOpen;

    private Button mButtonPause;

    private Button mButtonFinish;

    @Override protected final void onCreate( final Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        this.setContentView( R.layout.activity_main );

        this.initViews();
    }

    @Override protected final void onActivityResult( final int requestCode, final int resultCode, final Intent data )
    {
        if( resultCode == Activity.RESULT_OK && requestCode == SELECT_ANIMATE )
        {
            if( data == null || mButtonPause == null || mButtonFinish == null || mCheckReduce == null || mCheckSurface == null || mSurfaceView == null || mImageView == null ){ return; }

            mButtonPause.setText( R.string.BUTTON_PAUSE );

            mButtonPause.setEnabled( true );

            mButtonFinish.setText( R.string.BUTTON_FINISH );

            mButtonFinish.setEnabled( true );

            ////////////////////////////////////////////////////////////////////////////////////////

            mManager.setReduceSize( mCheckReduce.isChecked() );

            mManager.setView( mCheckSurface.isChecked() ? mSurfaceView : mImageView );

            mManager.setUri( data.getData() );  //NOTE:This call must after setView()

            mManager.restart( mManager.getInputStream() );
        }
    }

    @Override public final void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        if( mManager == null ){ return; }

        mManager.setTerminated();  //Can not add in onPause, because going home will terminate the renderer
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final void initViews()
    {
        mManager = new AnimateManager();

        mManager.setSurfaceBackground( Color.GRAY );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mSurfaceView = (SurfaceView)findViewById( R.id.surface_animate );

        mImageView = (ImageView)findViewById( R.id.image_animate );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mCheckReduce = (CheckBox)findViewById( R.id.check_reduce );

        mCheckReduce.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
        {
            @Override public final void onCheckedChanged( final CompoundButton buttonView, final boolean isChecked )
            {
                if( mManager == null ){ return; }

                mManager.setReduceSize( isChecked );
            }
        } );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mCheckSurface = (CheckBox)findViewById( R.id.check_surface_view );

        mCheckSurface.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
        {
            @Override public final void onCheckedChanged( final CompoundButton buttonView, final boolean isChecked )
            {
                if( mManager == null ){ return; }

                mManager.setView( isChecked ? mSurfaceView : mImageView );
            }
        } );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mButtonOpen = (Button)findViewById( R.id.button_open );

        mButtonOpen.setOnClickListener( new View.OnClickListener()
        {
            @Override public final void onClick( final View v )
            {
                selectAnimatedImage();
            }
        } );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mButtonPause = (Button)findViewById( R.id.button_pause );

        mButtonPause.setEnabled( false );

        mButtonPause.setOnClickListener( new View.OnClickListener()
        {
            @Override public final void onClick( final View v )
            {
                if( mManager == null || mButtonPause == null ){ return; }

                if( mManager.isPaused() )
                {
                    mButtonPause.setText( R.string.BUTTON_PAUSE );

                    mManager.setPaused( false );
                }
                else
                {
                    mButtonPause.setText( R.string.BUTTON_RESUME );

                    mManager.setPaused( true );
                }
            }
        } );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mButtonFinish = (Button)findViewById( R.id.button_finish );

        mButtonFinish.setEnabled( false );

        mButtonFinish.setOnClickListener( new View.OnClickListener()
        {
            @Override public final void onClick( final View v )
            {
                if( mManager == null || mButtonFinish == null ){ return; }

                if( mManager.isTerminated() )
                {
                    mButtonFinish.setText( R.string.BUTTON_FINISH );

                    mButtonPause.setText( R.string.BUTTON_PAUSE );

                    mButtonPause.setEnabled( true );

                    mManager.restart( mManager.getInputStream() );
                }
                else
                {
                    mButtonFinish.setText( R.string.BUTTON_REPLAY );

                    mButtonPause.setText( R.string.BUTTON_PAUSE );

                    mButtonPause.setEnabled( false );

                    mManager.setTerminated();
                }
            }
        } );
    }

    private final void selectAnimatedImage()
    {
        final Intent intent = new Intent( Intent.ACTION_GET_CONTENT );

        intent.setType( "image/*" );

        intent.putExtra( Intent.EXTRA_MIME_TYPES, new String[]{ "image/gif", "image/png", "image/apng" } );

        intent.addCategory( Intent.CATEGORY_OPENABLE );

        startActivityForResult( intent, SELECT_ANIMATE );
    }
}