package br.com.devmix.libs.hardware.camera;

import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.devmix.libs.camera.R;

public class Preview extends SurfaceView implements SurfaceHolder.Callback { // <1>
	    private static final String tag = "Preview";

	    SurfaceHolder mHolder;  // <2>
	    private static Camera camera; // <3>
	    public static Parameters parameters;
	    private static Context context;
	    private boolean init = true;
	    @SuppressWarnings("deprecation")
		Preview(Context context) {
	        super(context);

	        CameraActivity.freze = true;
	        Preview.context = context;
	        mHolder = getHolder();  // <4>
	        mHolder.addCallback(this);  // <5>
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // <6>
	    }


	    //Called once the holder is ready
	    @SuppressWarnings("deprecation")
		public void surfaceCreated(SurfaceHolder holder) {  // <7>
	    	if (getCamera() != null)return;
	    	
	        try{
	        	setCamera(Camera.open()); // <8>
	        	parameters = getCamera().getParameters();
		    	parameters.set("jpeg-quality", CameraActivity.jpgQuality);
		    	parameters.setPictureFormat(PixelFormat.JPEG);
		    	List<Size> suportedSizes = parameters.getSupportedPictureSizes();
		    	
		    	if(suportedSizes == null)return;
		    	
		    	//verificação de tamanhos suportados pelo dispositivo
		    	for(Size sizes:suportedSizes){
		    		Log.i(tag, "width: "+sizes.width);
		    		Log.i(tag, "height: "+sizes.height);
		    		if(sizes.width == 1024 && sizes.height == 768){
		    			parameters.setPictureSize(1024, 768);
		    			break;
		    		}else if(sizes.width == 800 && sizes.height == 600){
		    			parameters.setPictureSize(800, 600);
		    			break;
		    		}else if(sizes.width == 800 && sizes.height == 480){
		    			parameters.setPictureSize(800, 480);
		    			break;
		    		}else if(sizes.width == 640 && sizes.height == 480){
		    			parameters.setPictureSize(640, 480);
		    			break;
		    		}else if(sizes.width == 352 && sizes.height == 288){
		    			parameters.setPictureSize(352, 288);
		    			break;
		    		}else if(sizes.width == 320 && sizes.height == 240){
		    			parameters.setPictureSize(320, 240);
		    			break;
		    		}else if(sizes.width == 176 && sizes.height == 144){
		    			parameters.setPictureSize(176, 144);
		    			break;
		    		}else if(sizes.width == 160 && sizes.height == 120){
		    			parameters.setPictureSize(160, 120);
		    			break;
		    		}else if(sizes.width == 128 && sizes.height == 96){
		    			parameters.setPictureSize(128, 96);
		    			break;
		    		}else if(sizes.width == 88 && sizes.height == 72){
		    			parameters.setPictureSize(88, 72);
		    			break;
		    		}else if(sizes.width == 80 && sizes.height == 60){
		    			parameters.setPictureSize(80, 60);
		    			break;
		    		}
		    	}
		    	getCamera().setParameters(parameters);
		        try {
		            getCamera().setPreviewDisplay(holder);  // <9>

		            getCamera().setPreviewCallback(new Camera.PreviewCallback() { // <10>

		                public void onPreviewFrame(byte[] data, Camera camera) {  // <11>
		                    Preview.this.invalidate();  // <12>
		                }

		            });
		            init = true;
		        } catch (IOException e) { // <13>
		        	init = false;
		            e.printStackTrace();
		            new AlertDialog.Builder(context)
	        		.setTitle("Erro")
	        		.setCancelable(false)
	        		.setMessage(context.getResources().getString(R.string.error_code19))
	        		.setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							CameraActivity.finaliza();
						}
					})
	        		.create()
	        		.show();
		        }
	        }catch(Exception e){
	        	init = false;
	        	e.printStackTrace();
	        	new AlertDialog.Builder(context)
	        		.setTitle("Erro")
	        		.setCancelable(false)
	        		.setMessage(context.getResources().getString(R.string.error_code20))
	        		.setPositiveButton("Ok, vou encerrar", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							CameraActivity.finaliza();
						}
					})
					.setNegativeButton("Mais informações", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							maisInfo();
						}
					})
	        		.create()
	        		.show();
	        }
	    }
	    public void maisInfo(){
	    	new AlertDialog.Builder(context)
			.setTitle("Info")
			.setCancelable(false)
			.setMessage("Se você não conseguir fechar a aplicação que estiver utilizando a camera, será necessário reiniciar o smarthphone/tablet.")
			.setPositiveButton("Entendi",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						CameraActivity.finaliza();
					}
			}).create().show();
	    }
	    
	  @Override
	  public void surfaceDestroyed(SurfaceHolder holder) {  // <14>
	    	Log.i("in", "destruida");
		  releaseCamera();
	  }
	    public static void releaseCamera(){
	    	Log.i("in", "release");
	    	if (getCamera() != null) {
	        	getCamera().stopPreview();
	        	getCamera().setPreviewCallback(null);
	        	getCamera().release();
	        	setCamera(null);
	        }
	    }

	  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) { // <15>
		  if(init){
			  getCamera().startPreview();
			  CameraActivity.freze = false;
		  }
	  }

	static Camera getCamera() {
		return camera;
	}

	public static void setCamera(Camera camera) {
		Preview.camera = camera;
	}
}
