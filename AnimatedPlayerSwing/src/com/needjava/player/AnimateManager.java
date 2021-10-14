package com.needjava.player;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

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
public final class AnimateManager
{
    public static final int TYPE_UNKNOWN  = 0;

    public static final int TYPE_GIF      = 1;

    public static final int TYPE_APNG     = 2;

    private AnimateReadListener mListener;

    private File mFile;

    private int mType;

    private JLabel mView;

    private boolean mIsReduceSize;

    private boolean mIsSeekable;

    private AnimateReader mReader;

    private AnimateRenderer mRenderer;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final void setListener( final AnimateReadListener listener )
    {
        mListener = listener;
    }

    public final void setFile( final File file )
    {
        mFile = file;

        setType( getTypeFromSuffix( mFile ) );
    }

    public final void setType( final int type )  //NOTE:If no file extension, you have to call this
    {
        mType = type;
    }

    public final void setView( final JLabel view )
    {
        mView = view;
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
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final void restart( final InputStream input )
    {
        setTerminated();  //Must call this to avoid OOM

        mRenderer = new SwingAnimateRenderer( new SwingAnimateRendererListener() ); mRenderer.start();  //Step3. New renderer, start and waiting for decoding

        final AnimateBalancer balancer = new AnimateBalancer( mRenderer, mIsReduceSize );

        final Decoder decoder = ( mType == TYPE_GIF ? new DecoderGif( balancer ) : ( mType == TYPE_APNG ? new DecoderApng( balancer ) : null ) );

        mReader = new AnimateReader( mListener, input, decoder ); mReader.start();  //Step4. New reader, start and begin decoding
    }

    public final InputStream getInputStream()  //NOTE:Network should download GIF/APNG firstly, then try to open file from disk
    {
        try{ return new FileInputStream( mFile ); }

        catch( Exception e ){ e.printStackTrace(); return null; }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final int getTypeFromSuffix( final File file )
    {
        if( file == null ){ return TYPE_UNKNOWN; }

        final String path = file.getPath().toLowerCase();

        return ( path.endsWith( ".gif" ) ? TYPE_GIF : ( path.endsWith( ".png" ) ? TYPE_APNG : TYPE_UNKNOWN ) );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final class SwingAnimateRenderer extends AnimateRenderer
    {
        public SwingAnimateRenderer( final AnimateRendererListener listener )
        {
            super( listener, mIsSeekable, false/*HARDCODE*/ );
        }

        @Override public final boolean onNotifyRenderer( final AnimateFrame frame, final int frameCount, final int frameIndex, final boolean fromRenderer )
        {
            if( mView == null || frame == null ){ return false; }

            final BufferedImage image = getBufferedImageFromAnimateFrame( frame ); if( image == null ){ return false; }

            //try{ javax.imageio.ImageIO.write( image, "png", new java.io.File( new java.io.File( System.getProperty( "user.home" ), "Pictures" ), frameIndex + ".png" ) ); }catch( Exception e ){}

            SwingUtilities.invokeLater( new Runnable(){ @Override public final void run(){ mView.setIcon( new ImageIcon( image ) ); mView.repaint(); } } );

            return true;
        }

        private final BufferedImage getBufferedImageFromAnimateFrame( final AnimateFrame frame )
        {
            if( frame == null ){ return null; }

            final BufferedImage image = new BufferedImage( frame.mWidth, frame.mHeight, BufferedImage.TYPE_INT_ARGB );

            image.setRGB( 0, 0, frame.mWidth, frame.mHeight, frame.mColorPixels, 0, frame.mWidth );

            return image;
        }
    }

    private final class SwingAnimateRendererListener implements AnimateRendererListener
    {
        @Override public final void onRenderFinished( final boolean seekable, final boolean paused )
        {
            if( seekable ){ return; }  //It is not large GIF/APNG, just loop the frame list

            restart( getInputStream() );  //It is large GIF/APNG, decode the GIF/APNG file from the beginning
        }
    }
}