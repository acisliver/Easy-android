package com.example.easy_written;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Future;
import static android.os.Environment.DIRECTORY_PICTURES;

public class CV_record extends AppCompatActivity {
    //사진
    private static final int mREQUESTIMAGECAPTURE = 672;
    private String mImageFilePath;
    private Uri mPhotoUri;
    private ImageView mBtnCapture;
    private MediaScanner mMediaScanner; // 사진 저장 시 갤러리 폴더에 바로 반영사항을 업데이트 시켜주려면 이 것이 필요하다(미디어 스캐닝)
    private ImageView mPlayandSaveButton;
    private ArrayList<String> mPicturePathList;
    private DrawerLayout mDrawerLayout;

    //오디오
    ImageView mStartAndStopButton;
    String mAudiopathSave="";
    MediaRecorder mMediaRecorder;
    int mStartAndStopCheck=0;
    int mcheckPause=0;
    //private EditText mSaveFileName;

    //STT
    private static final String mSPEECHSUBSCRIPTIONKEY = "98ce7d7369024192aa438ba812b249c5";
    private static final String  mSERVICEREGION= "koreacentral";
    private String mSavedText = "";
    private SpeechRecognizer mReco;
    private boolean mContinuousListeningStarted = false;

    /*추가한 부분*/
    //sharedpreference에 카테고리를 저장
    //카테고리가 필요 할 때 마다 mCategoryArrayList에 저장하여 사용(이하 카테고리 배열이라고 하겠음)
    private ArrayList<String> mCategoryArrayList;
    private final String sharedPreferenceKey="saveArrayListToSharedPreference";
    /*끝*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_v_record);

        int mRequestCode = 5;//STT permission request code
        mPicturePathList=new ArrayList<>();

        /*추가한 부분*/
        //카테고리 배열 처음 부분에 '카테고리 선택' 추가
        //if를 사용한 것은 set을 이용해 0번째 배열에 접근을 해야 하지만, 배열에 아무것도 없을 경우 오류가 발생하기 때문에
        mCategoryArrayList=new ArrayList<>();
        Context mContext=getApplicationContext();
        mCategoryArrayList=getStringArrayPref(mContext,sharedPreferenceKey);
        if(mCategoryArrayList.size()==0) mCategoryArrayList.add("tmp");
        mCategoryArrayList.set(0,"기본 카테고리");
        setStringArrayPref(mContext,sharedPreferenceKey,mCategoryArrayList);
        /*끝*/

        //오디오
        mStartAndStopButton = findViewById(R.id.StartAndStopButton);
        mStartAndStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAndStop mPlayAndStop=new PlayAndStop();
                mPlayAndStop.execute();
                onSpeechButtonClicked();
            }
        });

        //저장버튼
        mPlayandSaveButton=findViewById(R.id.PlayandSaveButton);
        mPlayandSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder mAlert = new AlertDialog.Builder(CV_record.this);
                mAlert.setMessage("파일이름");


                /*추가한 부분*/
                //layout의 xml파일 중 dialog_spinner에 연결하여 다이어 로그창으로 사용용
               View mView=getLayoutInflater().inflate(R.layout.dialog_spinner,null);
                final EditText mSaveFileName=(EditText) mView.findViewById(R.id.saveFileName);
                final ImageView addCategory=(ImageView)mView.findViewById(R.id.addCategory);
                final Spinner dialogSpinner=(Spinner)mView.findViewById(R.id.dialogSpinner);



                //카테고리 배열에 카테고리를 불러옴
                mCategoryArrayList.clear();
                mCategoryArrayList=getStringArrayPref(mContext,sharedPreferenceKey);
                ArrayAdapter<String> mArrayAdapter=new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.support_simple_spinner_dropdown_item,mCategoryArrayList);
                mArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                dialogSpinner.setAdapter(mArrayAdapter);

                //dialog창에 구성요소들 추가
                mAlert.setView(mSaveFileName);
                mAlert.setView(addCategory);
                mAlert.setView(dialogSpinner);
                mAlert.setView(mView);

                //'+'버튼을 누르면 카토고리 추가 dialog창뜨게 함
                addCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder mCategoryAlert = new AlertDialog.Builder(CV_record.this);
                        EditText addCategoryEditText=new EditText(CV_record.this);
                        mCategoryAlert.setMessage("카테고리 이름");
                        mCategoryAlert.setView(addCategoryEditText);
                        mCategoryAlert.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //카테고리배열에 카테고리정보를 불러옴
                                mCategoryArrayList.clear();
                                mCategoryArrayList=getStringArrayPref(mContext,sharedPreferenceKey);
                                mCategoryArrayList.add(addCategoryEditText.getText().toString());
                                setStringArrayPref(mContext,sharedPreferenceKey,mCategoryArrayList);

                                //카테고리 생성시 스피너가 클릭되지 않는 버그가 있어서 강제로 스피너 refresh
                                ArrayAdapter<String> mCategoryArrayAdapter=new ArrayAdapter<String>(getApplicationContext(),
                                        R.layout.support_simple_spinner_dropdown_item,mCategoryArrayList);
                                mCategoryArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                                dialogSpinner.setAdapter(mCategoryArrayAdapter);
                            }
                        });
                        mCategoryAlert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog mCategoryAlertDialog=mCategoryAlert.create();
                        mCategoryAlertDialog.show();
                    }
                });

                //dialog창의 파일 저장 버튼(카테고리 아님)
                mAlert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //카테고리가 선택이 되었으면

                            CheckTypesTask mTypesTask=new CheckTypesTask();
                            //이전까지 파일 이름만 async에 보냈다면, 이제는 파일이 속한 카테고리의 정보까지 합쳐서 보냄(추후에 #으로 구분)
                            String mTmpString=mSaveFileName.getText().toString();
                            String mTmpCategory=dialogSpinner.getSelectedItem().toString();
                            String mPasstext=mTmpCategory+"#"+mTmpString;
                            mTypesTask.execute(mPasstext);

                    }
                });

                mAlert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"저장을 취소 했습니다.",Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog mAlertDialog = mAlert.create();

                mAlertDialog.show();
            }
        });
        /*끝*/


        // 사진 저장 후 미디어 스캐닝을 돌려줘야 갤러리에 반영됨.
        mMediaScanner = MediaScanner.getInstance(getApplicationContext());
        mBtnCapture=findViewById(R.id.btnCapture);
        mBtnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (mIntent.resolveActivity(getPackageManager()) != null) {
                    File mPhotoFile = null;
                    try {
                        mPhotoFile = createImageFile();
                    } catch (IOException e) {
                    }
                    if (mPhotoFile != null) {
                        mPhotoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), mPhotoFile);
                        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                        startActivityForResult(mIntent, mREQUESTIMAGECAPTURE);
                    }
                }
            }
        });

        //메뉴
        NavigationView mNavigationViewing = (NavigationView) findViewById(R.id.nav_view);
        View headerView = mNavigationViewing.getHeaderView(0);
        TextView mNavUsername = (TextView) headerView.findViewById(R.id.navi_user_id);
        mNavUsername.setText("easy");
        ImageView mNaviUserImage = headerView.findViewById(R.id.navi_user_image);
        Glide.with(this).load(R.drawable.easyicon2).into(mNaviUserImage);

        //액션바
        androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true); // 메뉴 버튼 만들기
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); //메뉴 버튼 이미지 지정

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                int mId = menuItem.getItemId();
                //파일 보기로 이동
                if(mId == R.id.goToFile){
                    Intent mintent=new Intent(getApplicationContext(),FileView.class);
                    startActivity(mintent);
                }
                return true;
            }
        });
    }
    public void onSpeechButtonClicked(){
        TextView mTxt = (TextView)findViewById(R.id.RecordText);

        if (mContinuousListeningStarted) {
            if (mReco != null) {
                final Future<Void> task = mReco.stopContinuousRecognitionAsync();
                //재생 버튼으로 바꿈
                mStartAndStopButton.setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_baseline_play_circle_filled_24, getApplicationContext().getTheme()));
//                //카메라, 저장 버튼 생성
//                mBtnCapture.setVisibility(View.VISIBLE);
//                mPlayandSaveButton.setVisibility(View.VISIBLE);
            } else {
                mContinuousListeningStarted = false;
            }
            mContinuousListeningStarted = false;
            return;
        }

        try {
            SpeechConfig mConfig = SpeechConfig.fromSubscription(mSPEECHSUBSCRIPTIONKEY, mSERVICEREGION);
            mConfig.setSpeechRecognitionLanguage("ko-KR");
            assert (mConfig!=null);
            mReco = new SpeechRecognizer(mConfig);
            assert (mReco!=null);
            mReco.recognizing.addEventListener((s, e) -> {
                if(e.getResult().getReason()==ResultReason.RecognizingSpeech){
                    mTxt.setText("" + mSavedText+e.getResult().getText());
                }
            });
            mReco.recognized.addEventListener((s, e) -> {
                if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                    mSavedText=mSavedText+ e.getResult().getText();
                    mTxt.setText("" + mSavedText);
                }
                else if (e.getResult().getReason() == ResultReason.NoMatch) {
                    System.out.println("NOMATCH: Speech could not be recognized.");
                }
            });
            mReco.canceled.addEventListener((s, e) -> {
                System.out.println("CANCELED: Reason=" + e.getReason());
                if (e.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + e.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + e.getErrorDetails());
                    System.out.println("CANCELED: Did you update the subscription info?");
                }
            });
            mReco.sessionStopped.addEventListener((s, e) -> {
                System.out.println("\n    Session stopped event.");
            });
            final Future<Void> mTask = mReco.startContinuousRecognitionAsync();
            assert(mTask != null);
            mContinuousListeningStarted = true;
            CV_record.this.runOnUiThread(() -> {
                //일시정지 버튼으로 바꿈
                mStartAndStopButton.setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_baseline_pause_circle_filled_24, getApplicationContext().getTheme()));
                mStartAndStopButton.setEnabled(true);
                //카메라, 저장 버튼 생성
                mBtnCapture.setVisibility(View.VISIBLE);
                mPlayandSaveButton.setVisibility(View.VISIBLE);
            });
            mReco.close();
        } catch (Exception ex) {
            assert(false);
        }
    }


    //스피닝 쓰레드
    private class CheckTypesTask extends AsyncTask<String,Void,Void> {
        ProgressDialog mAsyncDialog=new ProgressDialog(CV_record.this);
        @Override
        protected void onPreExecute() {
            mAsyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mAsyncDialog.setMessage("파일을 저장하고 있습니다.");
            mAsyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getApplicationContext(),"저장완료!!",Toast.LENGTH_SHORT).show();
            mAsyncDialog.dismiss();
            Intent mRestartIntent = getIntent();
            finish();
            startActivity(mRestartIntent);

            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... ReceivedFileName) {

            //저장시킬 파일 만들기
            String mReceivedFileNameToString=ReceivedFileName[0];
            SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
            Date mCurDate   = new Date(System.currentTimeMillis());
            String mFileDate  = mFormatter.format(mCurDate);
            String mRootDirectory=Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +"EASYWRITTEN";
            String mCreateFilePath= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +"EASYWRITTEN"+ "/"+ mReceivedFileNameToString+"#"+mFileDate;
            File mRootFile=new File(mRootDirectory);
            if(mRootFile.exists()) {
                //저장시킬 파일
                File mFile = new File(mCreateFilePath);
                if (!mFile.exists())
                    mFile.mkdirs();

                //저장시킬 파일로 오디오 경로 변경
                String mNewpathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "EASYWRITTEN" + "/" + mReceivedFileNameToString + "#" + mFileDate + "/" + "_audio_record" + ".3gp";
                RenameFile(mAudiopathSave, mNewpathSave);

                //사진 경로 변경
                for (int k = 0; k < mPicturePathList.size(); k++) {
                    String[] mStringSplit = (mPicturePathList.get(k)).split("#");
                    String mMovepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "EASYWRITTEN" + "/" + mReceivedFileNameToString + "#" + mFileDate + "/" + "#" + mStringSplit[1];
                    RenameFile(mPicturePathList.get(k), mMovepath);
                }

                //STT텍스트 저장
                FileOutputStream mFos = null;
                try {
                    mFos = new FileOutputStream(mCreateFilePath + "/" + "STTtext.txt", true);
                    BufferedWriter mWriter = new BufferedWriter(new OutputStreamWriter(mFos));
                    mWriter.write(mSavedText);
                    mWriter.flush();
                    mWriter.close();
                    mFos.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                publishProgress();
                if(mStartAndStopCheck==1){
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                }

            }else{
                Toast.makeText(getApplicationContext(),"파일 생성에 실패 했습니다.",Toast.LENGTH_SHORT).show();
                Splash splash=new Splash();
                splash.CreateFile(mRootDirectory);
            }
            return null;
        }
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private File createImageFile() throws IOException {
        String mTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String mImageFileName = "TEST_" + mTimeStamp + "_";
        File mStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mImage = File.createTempFile(
                mImageFileName,
                ".jpg",
                mStorageDir
        );
        mImageFilePath = mImage.getAbsolutePath();
        return mImage;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == mREQUESTIMAGECAPTURE && resultCode == RESULT_OK) {
            Bitmap mBitmap = BitmapFactory.decodeFile(mImageFilePath);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(mImageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int mExifOrientation;
            int mExifDegree;

            if (exif != null) {
                mExifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                mExifDegree = exifOrientationToDegress(mExifOrientation);
            } else {
                mExifDegree = 0;
            }

            String mResult = "";
            SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
            Date mCurDate   = new Date(System.currentTimeMillis());
            String mPictureDate  = mFormatter.format(mCurDate);
            String mStrFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + "/" + "EASYWRITTENPICTURE";
            File mFile = new File(mStrFolderName);
            if( !mFile.exists() )
                mFile.mkdirs();

            File mF = new File(mStrFolderName + "/" + "#" +mPictureDate + ".png");
            mPicturePathList.add(mF.getPath());

            FileOutputStream mFOut=null;
            try {
                mFOut = new FileOutputStream(mF);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mResult = "Save Error mFOut";
            }

            // 비트맵 사진 폴더 경로에 저장
            rotate(mBitmap,mExifDegree).compress(Bitmap.CompressFormat.PNG, 70, mFOut);

            try {
                mFOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mFOut.close();
                // 방금 저장된 사진을 갤러리 폴더 반영 및 최신화
                mMediaScanner.mediaScanning(mStrFolderName + "/" + mPictureDate + ".png");
            } catch (IOException e) {
                e.printStackTrace();
                mResult = "File close Error";
            }

        }
    }

    private int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix mMatrix = new Matrix();
        mMatrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mMatrix, true);
    }

    //녹음
    private void setupMediaRecorder() {
        mMediaRecorder=new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setOutputFile(mAudiopathSave);
    }

    public class PlayAndStop extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //시작
            if (mStartAndStopCheck == 0 && mcheckPause == 0) {
                    mAudiopathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ "_audio_record"+".3gp";
                    setupMediaRecorder();
                    try {
                        mMediaRecorder.prepare();
                        mMediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                mStartAndStopCheck=1;
            }
            //일시중지
            else if(mStartAndStopCheck==1 && mcheckPause == 0){
                try {
                    mMediaRecorder.pause();
                    mcheckPause=1;
                }
                catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
            //재시작
            else if(mStartAndStopCheck==1 && mcheckPause == 1){
                mMediaRecorder.resume();
                mStartAndStopCheck=1;
            }
            return null;
        }
    }

    public void RenameFile(String filename, String newFilename) {
        File mFile = new File( filename );
        File mFileNew = new File( newFilename );
        if( mFile.exists() ) mFile.renameTo( mFileNew );
    }

    //메뉴 탭에서 항목 선택시 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { // 왼쪽 상단 버튼 눌렀을 때
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /*추가한 부분*/
    //sharedPreference에 배열이 저장 될 수 있도록 하는 함수
    private void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();

        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.apply();
    }

    private ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
    /*끝*/
}
