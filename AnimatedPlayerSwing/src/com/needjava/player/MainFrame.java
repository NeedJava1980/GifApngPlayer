package com.needjava.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.awt.Dimension;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.filechooser.FileFilter;

import com.needjava.animate.AnimateReadListener;

/**
 * @author NeedJava1980@gmail.com 08/16/2017
 */
public final class MainFrame extends JFrame
{
    private static final long serialVersionUID            = 1L;

    private static final String CHECKBOX_REDUCE           = "Reduce Size";

    private static final String BUTTON_OPEN               = "Open";

    private static final String BUTTON_PAUSE              = "Pause";

    private static final String BUTTON_RESUME             = "Resume";

    private static final String BUTTON_FINISH             = "Finish";

    private static final String BUTTON_REPLAY             = "Replay";

    private static final String FRAME_TITLE               = "GIF/APNG Player v1.2 ©NeedJava1980@gmail.com";

    private static final String CHOOSER_TITLE             = "Select GIF/APNG file";

    private static final String STORED_LAST_PATH          = "STORED_LAST_PATH";

    private static final String STORED_LAST_PATH_FILE     = "last_path.txt";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private AnimateManager mManager;

    private JLabel mAnimateView;

    private JCheckBox mCheckReduceSize;

    private JButton mButtonOpen;

    private JButton mButtonPause;

    private JButton mButtonFinish;

    public static final void main( final String[] argv ) throws Exception
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            @Override public final void run()
            {
                final MainFrame frame = new MainFrame();

                frame.createViews();

                frame.setTitle( FRAME_TITLE );

                frame.setBounds( 200, 200, 600, 400 );

                frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

                frame.setVisible( true );
            }
        } );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final void createViews()
    {
        mManager = new AnimateManager();

        ////////////////////////////////////////////////////////////////////////////////////////////

        final JPanel buttonPane = new JPanel();

        buttonPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mAnimateView = new JLabel();

        mAnimateView.setHorizontalAlignment( JLabel.CENTER );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mCheckReduceSize = new JCheckBox( CHECKBOX_REDUCE );

        mCheckReduceSize.setPreferredSize( new Dimension( 180, 24 ) );

        mCheckReduceSize.setSelected( true );

        mCheckReduceSize.addChangeListener( new ChangeListener()
        {
            @Override public final void stateChanged( final ChangeEvent e )
            {
                if( mManager == null || mCheckReduceSize == null ){ return; }

                mManager.setReduceSize( mCheckReduceSize.isSelected() );
            }
        } );

        buttonPane.add( mCheckReduceSize );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mButtonOpen = new JButton( BUTTON_OPEN );

        mButtonOpen.setPreferredSize( new Dimension( 120, 24 ) );

        mButtonOpen.addActionListener( new ActionListener()
        {
            @Override public final void actionPerformed( final ActionEvent e )
            {
                final String storedLastPath = loadLastPath();

                final JFileChooser chooser = ( storedLastPath == null || storedLastPath.trim().length() == 0 ? new JFileChooser() : new JFileChooser( storedLastPath ) );

                chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

                chooser.setFileFilter( new AnimateFileFilter() );

                if( chooser.showDialog( MainFrame.this, CHOOSER_TITLE ) == JFileChooser.APPROVE_OPTION )
                {
                    final File file = chooser.getSelectedFile();

                    if( file == null || mManager == null || mButtonPause == null || mButtonFinish == null || mCheckReduceSize == null ){ return; }

                    saveLastPath( file.getParent() );

                    mButtonPause.setText( BUTTON_PAUSE );

                    mButtonPause.setEnabled( true );

                    mButtonFinish.setText( BUTTON_FINISH );

                    mButtonFinish.setEnabled( true );

                    ////////////////////////////////////////////////////////////////////////////////

                    mManager.setListener( new SwingAnimateReadListener() );

                    mManager.setFile( file );

                    mManager.setView( mAnimateView );

                    mManager.setReduceSize( mCheckReduceSize.isSelected() );

                    mManager.restart( mManager.getInputStream() );
                }
            }
        } );

        buttonPane.add( mButtonOpen );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mButtonPause = new JButton( BUTTON_PAUSE );

        mButtonPause.setPreferredSize( new Dimension( 120, 24 ) );

        mButtonPause.setEnabled( false );

        mButtonPause.addActionListener( new ActionListener()
        {
            @Override public final void actionPerformed( final ActionEvent e )
            {
                if( mManager == null || mButtonPause == null ){ return; }

                if( mManager.isPaused() )
                {
                    mButtonPause.setText( BUTTON_PAUSE );

                    mManager.setPaused( false );
                }
                else
                {
                    mButtonPause.setText( BUTTON_RESUME );

                    mManager.setPaused( true );
                }
            }
        } );

        buttonPane.add( mButtonPause );

        ////////////////////////////////////////////////////////////////////////////////////////////

        mButtonFinish = new JButton( BUTTON_FINISH );

        mButtonFinish.setPreferredSize( new Dimension( 120, 24 ) );

        mButtonFinish.setEnabled( false );

        mButtonFinish.addActionListener( new ActionListener()
        {
            @Override public final void actionPerformed( final ActionEvent e )
            {
                if( mManager == null || mButtonFinish == null || mButtonPause == null ){ return; }

                if( mManager.isTerminated() )
                {
                    mButtonFinish.setText( BUTTON_FINISH );

                    mButtonPause.setText( BUTTON_PAUSE );

                    mButtonPause.setEnabled( true );

                    mManager.restart( mManager.getInputStream() );
                }
                else
                {
                    mButtonFinish.setText( BUTTON_REPLAY );

                    mButtonPause.setText( BUTTON_PAUSE );

                    mButtonPause.setEnabled( false );

                    mManager.setTerminated();
                }
            }
        } );

        buttonPane.add( mButtonFinish );

        ////////////////////////////////////////////////////////////////////////////////////////////

        getContentPane().add( BorderLayout.CENTER, mAnimateView );

        getContentPane().add( BorderLayout.SOUTH, buttonPane );
    }

    private final String loadLastPath()
    {
        FileInputStream input = null;

        try
        {
            final Properties prop = new Properties();

            prop.load( input = new FileInputStream( STORED_LAST_PATH_FILE ) );

            return prop.getProperty( STORED_LAST_PATH );
        }
        catch( Exception e )
        {
            return null;
        }
        finally
        {
            try{ input.close(); }

            catch( Exception ex ){}
        }
    }

    private final void saveLastPath( final String lastPath )
    {
        FileOutputStream output = null;

        try
        {
            final Properties prop = new Properties();

            prop.setProperty( STORED_LAST_PATH, lastPath );

            prop.store( output = new FileOutputStream( STORED_LAST_PATH_FILE ), FRAME_TITLE );
        }
        catch( Exception e )
        {
            //Do nothing
        }
        finally
        {
            try{ output.close(); }

            catch( Exception ex ){}
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final class AnimateFileFilter extends FileFilter
    {
        @Override public final String getDescription(){ return "*.gif  *.png"; }

        @Override public final boolean accept( final File file )
        {
            if( file == null ){ return false; }

            final String fileName = file.getName().toLowerCase();

            return ( file.isDirectory() || fileName.endsWith( ".gif" ) || fileName.endsWith( ".png" ) );
        }
    }

    private final class SwingAnimateReadListener implements AnimateReadListener
    {
        @Override public final void onReadFailed( final int status, final Object object )
        {
            //TODO:If failed to animate images, do something like closing player, closing window

            System.err.println( object );
        }
    }
}