package br.com.devmix.libs.hardware.camera;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.devmix.libs.camera.R;
import com.devmix.libs.utils.files.FileManager;
/**
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />]
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature
        android:name="android.hardware.camera"
        android:required="true" />
    <activity
        android:name="br.com.devmix.libs.hardware.camera.CameraActivity"
        android:screenOrientation="landscape"
         />
 * @author echer
 *
 */
public class CameraActivity extends Activity {
	private static final String TAG = CameraActivity.class.getName();
	public static final int REQUEST_CODE = 100;
	Preview preview;
	private static FrameLayout buttonClick;
	private static Activity activity;
	public static boolean freze = false;
	private Intent intent;

	/**
	 * extra values
	 */
	public static final String EXTRA_SD_CARD_DIRECTORY_TO_WRITE = "directory";
	public static final String EXTRA_JPG_FILE_NAME = "jpg_file_name";
	public static final String EXTRA_JPG_QUALITY = "jpg_quality";
	public static int jpgQuality = 75;
	private String diretorioPadrao = "PHOTO";
	private String fileName = "tmp";

	/**
	 * return values
	 */
	public static final String RETURN_FOTO_SUCESSFULL = "sucess";
	public static final String RETURN_BACK_PRESSED = "back_pressed";
	public static final String RETURN_KEY = "return_error_code";
	private int errorCode = 0;


	@Override
	public void finish() {
		Intent data = new Intent();
		if(errorCode != 0){
		  switch (errorCode) {
		  	case 1:
				data.putExtra(RETURN_KEY, this.getResources().getString(R.string.error_camera_write_file));
				break;
			case 2:
				data.putExtra(RETURN_KEY, RETURN_BACK_PRESSED);
				break;
			case 3:
				data.putExtra(RETURN_KEY, this.getResources().getString(R.string.error_camera_sd_card_fault));
				break;
			case 4:
				data.putExtra(RETURN_KEY, String.format(this.getResources().getString(R.string.error_camera_mkdir_fail), diretorioPadrao));
				break;
			default:
				data.putExtra(RETURN_KEY, this.getResources().getString(R.string.error_camera_error_destroy));
				break;
			}
		 }else{
			 data.putExtra(RETURN_KEY, RETURN_FOTO_SUCESSFULL);
		 }
	  setResult(RESULT_OK, data);
	  super.finish();
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;

		setContentView(R.layout.camera);

		intent = getIntent();

		if(!intent.hasExtra(EXTRA_SD_CARD_DIRECTORY_TO_WRITE)){
			Log.i(TAG, "Diretório no cartão SD para gravação não especificado, assumindo pasta padrão: "+diretorioPadrao);
		}else{
			diretorioPadrao = intent.getStringExtra(EXTRA_SD_CARD_DIRECTORY_TO_WRITE);
		}
		if(!intent.hasExtra(EXTRA_JPG_FILE_NAME)){
			Log.i(TAG, "Nome de arquivo para gravação não específicado, assumindo nome padrão: "+fileName+".jpeg");
		}else{
			fileName = intent.getStringExtra(EXTRA_JPG_FILE_NAME);
		}


		preview = new Preview(this); // <3>
		((FrameLayout) findViewById(R.id.preview)).addView(preview); // <4>

		Toast.makeText(getBaseContext(), "Toque a tela para tirar a foto.",
				Toast.LENGTH_LONG).show();
		buttonClick = (FrameLayout) findViewById(R.id.preview);
		buttonClick.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { // <5>
				tiraFoto();
			}
		});
	}
	protected void tiraFoto() {
		if(!freze){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			freze = true;
			Preview.getCamera().takePicture(shutterCallback, rawCallback,
					jpegCallback);
		}
	}
	public void cameraVoltar_onClick(View v){
		errorCode = 2;
		finaliza();
	}
	public void tiraFoto_onClick(View v){
		tiraFoto();
	}
	@Override
	public void onBackPressed(){
		errorCode = 2;
		finaliza();
	}

	@Override
	protected void onPause() {
		Preview.releaseCamera();
		super.onPause();

	}

	public static void finaliza() {
		Preview.releaseCamera();
		activity.finish();
	}


	ShutterCallback shutterCallback = new ShutterCallback() { // <6>
		public void onShutter() {
		}
	};

	PictureCallback rawCallback = new PictureCallback() { // <7>
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	PictureCallback jpegCallback = new PictureCallback() { // <8>
		public void onPictureTaken(byte[] data, Camera camera) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				if (FileManager
						.diretorioExiste(Environment
								.getExternalStorageDirectory()
								+ "/"
								+ diretorioPadrao)) {
					gravaArquivo(data);
				} else {
					// cria diretorio
					if (FileManager.criaDiretorio(Environment
							.getExternalStorageDirectory().toString(),
							diretorioPadrao)) {
						gravaArquivo(data);
					} else{
						errorCode = 4;
						finish();
					}
				}
			} else {
				novoDialogoErro(
						"Erro",
						CameraActivity.this.getResources().getString(R.string.error_camera_sd_card_fault),
						"Ok, vou inseri-lo");
			}
		}
	};


	protected void gravaArquivo(byte[] data) {
		FileOutputStream outStream = null;
		String pathToSave = Environment.getExternalStorageDirectory() + "/"
				+ diretorioPadrao + "/" +fileName+".jpeg";
		try {
			outStream = new FileOutputStream(pathToSave);
			outStream.write(data);
			outStream.close();
			errorCode = 0;
			finish();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			errorCode = 1;
			finish();
		} catch (IOException e) {
			e.printStackTrace();
			errorCode = 1;
			finish();
		}

	}
	protected void novoDialogoErro(String titulo, String message,
			String positBtn) {
		new AlertDialog.Builder(CameraActivity.this)
			.setTitle(titulo)
			.setCancelable(false)
			.setMessage(message)
			.setPositiveButton(positBtn,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						errorCode = 3;
						finish();
					}
				}).create().show();
	}

	@Override
	protected void onDestroy() {
		Preview.releaseCamera();
		super.onDestroy();
	}
}