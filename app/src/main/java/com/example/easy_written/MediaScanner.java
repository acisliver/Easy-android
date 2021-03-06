package com.example.easy_written;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 이미지 저장 후 미디어 스캐닝을 수행해줄 때 사용하는 유틸 클래스
 */
public class MediaScanner {
    private                 Context                                             mContext;
    private static volatile MediaScanner                                        mMediaInstance = null;
    private                 MediaScannerConnection                              mMediaScanner;
    private String mFilePath;

    public static MediaScanner getInstance( Context context ) {
        if(  context ==null)
            return null;

        if(  mMediaInstance ==null)
            mMediaInstance = new MediaScanner( context );
        return mMediaInstance;
    }

    public static void releaseInstance() {
        if ( mMediaInstance !=null) {
            mMediaInstance = null;
        }
    }


    private MediaScanner(Context context) {
        mContext = context;
        mFilePath = "";

        MediaScannerConnection.MediaScannerConnectionClient mMediaScanClient;
        mMediaScanClient = new MediaScannerConnection.MediaScannerConnectionClient(){
            @Override public void onMediaScannerConnected() {
                mMediaScanner.scanFile(mFilePath, null);
            }

            @Override public void onScanCompleted(String path, Uri uri) {
                System.out.println("::::MediaScan Success::::");

                mMediaScanner.disconnect();
            }
        };
        mMediaScanner = new MediaScannerConnection(mContext, mMediaScanClient);
    }

    public void mediaScanning(final String path) {
        if( TextUtils.isEmpty(path) )
            return;
        mFilePath = path;
        if( !mMediaScanner.isConnected() )
            mMediaScanner.connect();
    }
}
