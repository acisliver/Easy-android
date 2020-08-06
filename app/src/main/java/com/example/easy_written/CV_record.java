package com.example.easy_written;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

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
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilePath;
    private Uri photoUri;
    private Button btn_capture;
    private MediaScanner mMediaScanner; // 사진 저장 시 갤러리 폴더에 바로 반영사항을 업데이트 시켜주려면 이 것이 필요하다(미디어 스캐닝)
    private Button PlayandSaveButton;
    private ArrayList<String> PicturePathList;

    //오디오
    Button StartAndStopButton;
    String AudiopathSave="";
    MediaRecorder mediaRecorder;
    final int REQUEST_AUDIO_PEMISSION_CODE=1000;
    int StartAndStopCheck=1;
    EditText SaveFileName;

    //STT
    private static final String SPEECHSUBSCRIPTIONKEY = "98ce7d7369024192aa438ba812b249c5";
    private static final String  SERVICEREGION= "koreacentral";
    private String saved_text = "";
    private SpeechRecognizer reco;
    private boolean continuousListeningStarted = false;


    //기타
    private Spinner spinner2;
    ArrayList<String> arrayList;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_v_record);

        int requestCode = 5;//STT permission request code

        PicturePathList=new ArrayList<>();

        //textbox
//        TextView RecordText=findViewById(R.id.RecordText);
//        RecordText.setText("만나서 반갑습니다. ***교수입니다. 오늘의 주제는 안드로이드 입니다. 안드로이드는 구글에서 만든 스마트폰용 운영체제입니다. 운영체제와 미들웨어, 사용자 인터페이스, 어플리케이션, MMS 서비스 등을 하나로 묶어 서비스를 제공하며 다양한 어플리케이션을 만들어 설치하면 실행될 수 있도록 구성된 어플리케이션 플랫폼이라고도 볼 수 있습니다. 많은 사람들이 iOS(애플 운영체제)에 견주어 스마트폰과 태블릿으로 안드로이드 운영체제를 사용하면서, 안드로이드는 세계 모바일 시장에서 가장 성공한 OS라는 평가를 받고있습니다. 안드로이드는 리눅스(Linux)를 기반으로 제작되었고 언어는 자바를 사용합니다.  ");

        //배속 설정
        arrayList = new ArrayList<>();
        arrayList.add("1.0배속");
        arrayList.add("1.2배속");
        arrayList.add("1.5배속");
        arrayList.add("1.7배속");
        arrayList.add("2.0배속");

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                arrayList);

        spinner2 = (Spinner)findViewById(R.id.spinner2);
        spinner2.setAdapter(arrayAdapter);

        //오디오
        StartAndStopButton = findViewById(R.id.StartAndStopButton);
        StartAndStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAndStop playAndStop=new PlayAndStop();
                playAndStop.execute();
                onSpeechButtonClicked();
            }
        });

        //종료버튼
        PlayandSaveButton=findViewById(R.id.PlayandSaveButton);
        PlayandSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(CV_record.this);
                alert.setMessage("파일이름");

                SaveFileName=new EditText(CV_record.this);
                alert.setView(SaveFileName);

                alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CheckTypesTask typesTask=new CheckTypesTask();
                        String Passtext=SaveFileName.getText().toString();
                        typesTask.execute(Passtext);
                    }
                });

                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"저장을 취소 했습니다.",Toast.LENGTH_SHORT).show();
                    }
                });

                alert.show();

                Message msg=new Message();
                msg.obj=SaveFileName.getText();
            }
        });


        // 사진 저장 후 미디어 스캐닝을 돌려줘야 갤러리에 반영됨.
        mMediaScanner = MediaScanner.getInstance(getApplicationContext());

        btn_capture=findViewById(R.id.btn_capture);
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {

                    }
                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
    }


    //스피닝 쓰레드,저장
    private class CheckTypesTask extends AsyncTask<String,Void,Void> {

        ProgressDialog asyncDialog=new ProgressDialog(CV_record.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("파일을 저장하고 있습니다.");
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            asyncDialog.dismiss();
            onBackPressed();
            super.onPostExecute(aVoid);
        }


        @Override
        protected Void doInBackground(String... ReceivedFileName) {

            setupMediaRecorder();
            //저장시킬 파일 만들기
            String ReceivedFileNameToString=ReceivedFileName[0];
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
            Date curDate   = new Date(System.currentTimeMillis());
            String fileDate  = formatter.format(curDate);
            String CreateFilePath= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +"EASYWRITTEN"+ "/"+ ReceivedFileNameToString+"#"+fileDate;
            File file=new File(CreateFilePath);
            if(!file.exists())
                file.mkdirs();

            //저장시킬 파일로 오디오 경로 변경
            String NewpathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+"EASYWRITTEN"+ "/"+ReceivedFileNameToString+"#"+fileDate+ "/"+"_audio_record"+".3gp";
            renameFile(AudiopathSave,NewpathSave);

            //사진 경로 변경
            for(int k=0;k<PicturePathList.size();k++){
                String[] StringSplit=(PicturePathList.get(k)).split("#");
                String Movepath=Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+"EASYWRITTEN"+ "/"+ReceivedFileNameToString+"#"+fileDate+"/"+"#"+StringSplit[1];
                renameFile(PicturePathList.get(k),Movepath);
            }

            //STT텍스트 저장
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(CreateFilePath+"/"+"STTtext.txt", true);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                writer.write(saved_text);
                writer.flush();
                writer.close();
                fos.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegress(exifOrientation);
            } else {
                exifDegree = 0;
            }

            String result = "";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
            Date curDate   = new Date(System.currentTimeMillis());
            String PictureDate  = formatter.format(curDate);

            String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + "/" + "EASYWRITTENPICTURE";
            File file = new File(strFolderName);
            if( !file.exists() )
                file.mkdirs();

            File f = new File(strFolderName + "/" + "#" +PictureDate + ".png");
            //result=f.getPath()
            PicturePathList.add(f.getPath());

            FileOutputStream fOut=null;
            try {
                fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = "Save Error fOut";
            }

            // 비트맵 사진 폴더 경로에 저장
            rotate(bitmap,exifDegree).compress(Bitmap.CompressFormat.PNG, 70, fOut);

            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
                // 방금 저장된 사진을 갤러리 폴더 반영 및 최신화
                mMediaScanner.mediaScanning(strFolderName + "/" + PictureDate + ".png");
            } catch (IOException e) {
                e.printStackTrace();
                result = "File close Error";
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
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //녹음
    private void setupMediaRecorder() {
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        mediaRecorder.setOutputFile(AudiopathSave);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_AUDIO_PEMISSION_CODE:
            {
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,"permission granted",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private boolean checkPermissionFromDevice(){
        int write_external_storage_resuly= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result=ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        return write_external_storage_resuly== PackageManager.PERMISSION_GRANTED && record_audio_result==PackageManager.PERMISSION_GRANTED;

    }

    public class PlayAndStop extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (StartAndStopCheck == 1) {
                if (checkPermissionFromDevice()) {
                    AudiopathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +"EASYWRITTEN"+ "/"+ "_audio_record"+".3gp";

                    setupMediaRecorder();
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                }
                StartAndStopCheck=0;
            }
            else if(StartAndStopCheck==0){
                try {
                    mediaRecorder.pause();
                    StartAndStopCheck=1;
                }
                catch (IllegalStateException e){
                    e.printStackTrace();
                }

            }
            return null;
        }
    }

    //파일 경로 변경
    public void renameFile(String filename, String newFilename) {
        File file = new File( filename );
        File fileNew = new File( newFilename );
        if( file.exists() ) file.renameTo( fileNew );
    }

    //실행/정지 버튼 클릭 시 STT
    public void onSpeechButtonClicked(){
        TextView txt = (TextView)findViewById(R.id.RecordText);

        if (continuousListeningStarted) {
            if (reco != null) {
                final Future<Void> task = reco.stopContinuousRecognitionAsync();
                StartAndStopButton.setText("START");
            } else {
                continuousListeningStarted = false;
            }
            continuousListeningStarted = false;

            return;
        }

        try {
            SpeechConfig config = SpeechConfig.fromSubscription(SPEECHSUBSCRIPTIONKEY, SERVICEREGION);
            config.setSpeechRecognitionLanguage("ko-KR");
            assert (config!=null);

            reco = new SpeechRecognizer(config);
            assert (reco!=null);


            reco.recognizing.addEventListener((s, e) -> {
                if(e.getResult().getReason()==ResultReason.RecognizedSpeech){
                    System.out.println("RECOGNIZING: Text=" +saved_text+ e.getResult().getText());
                    txt.setText("RECOGNIZING: Text=" + saved_text+e.getResult().getText());
                }

            });
            reco.recognized.addEventListener((s, e) -> {
                if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                    saved_text=saved_text+ e.getResult().getText();
                    System.out.println("RECOGNIZED: Text="+saved_text);
                    txt.setText("RECOGNIZED: Text=" + saved_text);
                }
                else if (e.getResult().getReason() == ResultReason.NoMatch) {
                    System.out.println("NOMATCH: Speech could not be recognized.");
                }
            });

            reco.canceled.addEventListener((s, e) -> {
                System.out.println("CANCELED: Reason=" + e.getReason());

                if (e.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + e.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + e.getErrorDetails());
                    System.out.println("CANCELED: Did you update the subscription info?");
                }

            });

            reco.sessionStopped.addEventListener((s, e) -> {
                System.out.println("\n    Session stopped event.");
            });
            final Future<Void> task = reco.startContinuousRecognitionAsync();
            assert(task != null);
            continuousListeningStarted = true;
            CV_record.this.runOnUiThread(() -> {
                StartAndStopButton.setText("Stop");
                StartAndStopButton.setEnabled(true);
            });

            Log.i("stop","stop");
            reco.close();
        } catch (Exception ex) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.getMessage());
            assert(false);
        }
    }
}
