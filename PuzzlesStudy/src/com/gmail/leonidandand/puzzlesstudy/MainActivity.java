package com.gmail.leonidandand.puzzlesstudy;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.gmail.leonidandand.puzzlesstudy.difficulty.DimensionLoader;
import com.gmail.leonidandand.puzzlesstudy.utils.Dimension;

public class MainActivity extends Activity {

	private static final String[] difficultyNames = new String[] { "Easy", "Medium", "Hard" };
	
	private static final int PICK_IMAGE = 42345;

	private Spinner difficultyPicker;
	private DimensionLoader dimensionLoader;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageGenerator.getInstance().setResources(getResources());
		ResourceReader.init(getResources());
		setContentView(R.layout.activity_main);
		dimensionLoader = new DimensionLoader(getResources());
		initSpinner();
		findViewById(R.id.pickImageButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onPickAndShowImage();
			}
		});
		
		findViewById(R.id.randomImageButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onShowRandomImage();
			}
		});
	}

	private void initSpinner() {
		difficultyPicker = (Spinner) findViewById(R.id.difficultyPicker);
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(
        		this, android.R.layout.simple_spinner_item, difficultyNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultyPicker.setAdapter(adapter);
        difficultyPicker.setSelection(0);
	}
	
	private void onShowRandomImage() {
		try {
			ImageGenerator generator = ImageGenerator.getInstance();
			Bitmap bitmap = generator.randomImage();
			setBitmap(bitmap);
		} catch (OutOfMemoryError e) {
			Toast.makeText(this, "Sorry. Image too big. Please, pick other image.",
						Toast.LENGTH_SHORT).show();
		}
	}
	
	private void setBitmap(Bitmap bitmap) {
		int position = difficultyPicker.getSelectedItemPosition();
		if (position == AdapterView.INVALID_POSITION) {
			Toast.makeText(this, "Please, pick difficulty", Toast.LENGTH_SHORT).show();
		} else {
			Dimension dim = dimensionLoader.dimension(difficultyNames[position]);
			startGame(bitmap, dim);
		}
	}

	private void startGame(Bitmap bitmap, Dimension dim) {
		SaverLoader.save("bitmap", bitmap);
		SaverLoader.save("dim", dim);
		Intent intent = new Intent(this, StarterActivity.class);
		startActivity(intent);
	}

	private void onPickAndShowImage() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
			Uri uri = data.getData();
			if (uri != null) {
				onImagePicked(uri);
			}
		}
	}

	private void onImagePicked(Uri uri) {
		String imageFilePath = imageFilePathByUri(uri);
		try {
			Bitmap bitmap = getBitmapByPath(imageFilePath);
			setBitmap(bitmap);
		} catch (Exception e) {
			String message = "Cannot load image. Please, choose other image.\n" + e.getMessage();
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
	}

	private String imageFilePathByUri(Uri uri) {
		String[] projection = new String[] { android.provider.MediaStore.Images.ImageColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        cursor.moveToFirst();
        String imageFilePath = cursor.getString(0);
        cursor.close();
        return imageFilePath;
	}
	
	private Bitmap getBitmapByPath(String imageFilePath) {
		File imgFile = new File(imageFilePath);
		return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	}

}
