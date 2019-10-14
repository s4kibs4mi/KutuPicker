package ninja.sakib.kutupicker.activities;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

import ninja.sakib.kutupicker.R;
import ninja.sakib.kutupicker.adapters.KutuPicuSelectedImageAdapter;
import ninja.sakib.kutupicker.listeners.OnItemRemovedListener;
import ninja.sakib.kutupicker.models.SelectedImage;
import ninja.sakib.kutupicker.utils.CodeUtil;
import ninja.sakib.kutupicker.utils.ImageUtil;
import ninja.sakib.kutupicker.views.CameraView;

public class CameraPickerActivity extends AppCompatActivity implements OnItemRemovedListener {
    private FrameLayout cameraPreview;
    private CameraView cameraView;
    private Camera camera;

    private RecyclerView selectedImagesView;
    private KutuPicuSelectedImageAdapter selectedImageAdapter;
    private ArrayList<SelectedImage> selectedImages;

    private ImageView captureBtn;
    private ImageView switchCameraBtn;
    private boolean isBackCameraActive = true;

    private int maxSelection = -1;
    private int minSelection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_picker);

        cameraPreview = findViewById(R.id.camera_preview);
        captureBtn = findViewById(R.id.capture_image);
        switchCameraBtn = findViewById(R.id.switch_camera);

        selectedImages = new ArrayList<>();
        selectedImageAdapter = new KutuPicuSelectedImageAdapter(getApplicationContext(), selectedImages, this);
        selectedImagesView = findViewById(R.id.camera_selected_images);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        selectedImagesView.setLayoutManager(linearLayoutManager);
        selectedImagesView.setAdapter(selectedImageAdapter);

        showTitle();
        parseIntentParams();

        askCameraPermission();
    }

    private void parseIntentParams() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            maxSelection = bundle.getInt(CodeUtil.MAX_SELECTION, -1);
            minSelection = bundle.getInt(CodeUtil.MIN_SELECTION, -1);
        }
    }

    private void askCameraPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        askStorageWritePermission();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    private void askStorageWritePermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        int backCameraId = findBackCameraId();
                        setCamera(backCameraId);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    private void showTitle() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Take Pictures");
            actionBar.setDisplayHomeAsUpEnabled(true);
            return;
        }

        androidx.appcompat.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Take Pictures");
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onNavigateUp() {
        onImageSelectionBackPressed();
        return super.onNavigateUp();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onImageSelectionBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onImageSelectionBackPressed();
    }

    private void onImageSelectionDone() {
        if (minSelection != -1 && selectedImages.size() < minSelection) {
            String msg = "Choose minimum " + minSelection + " images";
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            return;
        }

        releaseCamera();

        ArrayList<String> images = new ArrayList<>();
        for (SelectedImage selectedImage : selectedImages) {
            images.add(selectedImage.getImage());
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(CodeUtil.SELECTED_IMAGES_KEY, images);
        setResult(CodeUtil.IMAGE_SELECTION_DONE, resultIntent);
        finish();
    }

    private void onImageSelectionCancelled() {
        ArrayList<String> images = new ArrayList<>();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(CodeUtil.SELECTED_IMAGES_KEY, images);
        setResult(CodeUtil.IMAGE_SELECTION_CANCELLED, resultIntent);
        finish();
    }

    private void onImageSelectionClear() {
        selectedImages.clear();
        selectedImageAdapter.notifyDataSetChanged();
    }

    private void onImageSelectionBackPressed() {
        releaseCamera();

        ArrayList<String> images = new ArrayList<>();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(CodeUtil.SELECTED_IMAGES_KEY, images);
        setResult(CodeUtil.IMAGE_SELECTION_BACK_PRESSED, resultIntent);
        finish();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    private void setCamera(int cameraId) {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }

        camera = Camera.open(cameraId);
        camera.getParameters().setPreviewFormat(ImageFormat.RGB_565);
        camera.setDisplayOrientation(90);

        cameraView = new CameraView(getApplicationContext(), camera);
        cameraView.refreshCamera(camera);
        cameraPreview.addView(cameraView);

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
        switchCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
    }

    private void switchCamera() {
        if (isBackCameraActive) {
            int cameraId = findFrontCameraId();
            if (cameraId == -1) {
                Toast.makeText(getApplicationContext(), "No front camera found", Toast.LENGTH_SHORT).show();
                return;
            }
            setCamera(cameraId);
        } else {
            int cameraId = findBackCameraId();
            setCamera(cameraId);
        }

        isBackCameraActive = !isBackCameraActive;
    }

    private void refreshCamera() {
        if (!isBackCameraActive) {
            int cameraId = findFrontCameraId();
            if (cameraId == -1) {
                Toast.makeText(getApplicationContext(), "No front camera found", Toast.LENGTH_SHORT).show();
                return;
            }
            setCamera(cameraId);
        } else {
            int cameraId = findBackCameraId();
            setCamera(cameraId);
        }
    }

    private void captureImage() {
        camera.takePicture(null, null, getPictureCallback());
    }

    private int findBackCameraId() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;

            }

        }
        return cameraId;
    }

    private int findFrontCameraId() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("Where", "Picture Taken");

                if (maxSelection != -1 && selectedImages.size() >= maxSelection) {
                    Toast.makeText(getApplicationContext(), "Maximum images selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                String imagePath = ImageUtil.saveToStorage(bitmap);

                if (imagePath != null) {
                    selectedImages.add(new SelectedImage(imagePath));
                    selectedImageAdapter.notifyDataSetChanged();

                    Log.d("Returned ImagePath", imagePath);
                }
                refreshCamera();
            }
        };
        return picture;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode, data);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);

        MenuItem doneMenuItem = menu.findItem(R.id.menu_item_done);
        doneMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onImageSelectionDone();
                return false;
            }
        });

        MenuItem cameraMenuItem = menu.findItem(R.id.menu_item_switch_to_camera);
        cameraMenuItem.setVisible(false);

        MenuItem galleryMenuItem = menu.findItem(R.id.menu_item_switch_to_gallery);
        galleryMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                releaseCamera();

                Intent i = new Intent(getApplicationContext(), GalleryPickerActivity.class);
                startActivityForResult(i, CodeUtil.IMAGE_SELECTION_REQUEST_CODE);
                return false;
            }
        });

        MenuItem cancelMenuItem = menu.findItem(R.id.menu_item_cancel);
        cancelMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onImageSelectionCancelled();
                return false;
            }
        });

        MenuItem clearMenuItem = menu.findItem(R.id.menu_item_clear);
        clearMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onImageSelectionClear();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRemoved(SelectedImage selectedImage) {
        selectedImages.remove(selectedImage);
        selectedImageAdapter.notifyDataSetChanged();
    }
}
