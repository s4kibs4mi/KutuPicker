package ninja.sakib.kutupicker.activities;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
import ninja.sakib.kutupicker.adapters.KutuPicuLoadedImageAdapter;
import ninja.sakib.kutupicker.adapters.KutuPicuSelectedImageAdapter;
import ninja.sakib.kutupicker.listeners.OnItemRemovedListener;
import ninja.sakib.kutupicker.listeners.OnItemSelectedListener;
import ninja.sakib.kutupicker.models.LoadedImage;
import ninja.sakib.kutupicker.models.SelectedImage;
import ninja.sakib.kutupicker.utils.CodeUtil;
import ninja.sakib.kutupicker.utils.ImageUtil;

public class GalleryPickerActivity extends AppCompatActivity implements PermissionListener, OnItemSelectedListener, OnItemRemovedListener {
    private RecyclerView loadedImagesView;
    private KutuPicuLoadedImageAdapter loadedImageAdapter;
    private ArrayList<LoadedImage> loadedImages;

    private RecyclerView selectedImagesView;
    private KutuPicuSelectedImageAdapter selectedImageAdapter;
    private ArrayList<SelectedImage> selectedImages;

    private int page = 1;
    private int limit = 25;

    private int maxSelection = -1;
    private int minSelection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_picker);

        showTitle();
        parseIntentParams();

        loadedImages = new ArrayList<>();
        loadedImageAdapter = new KutuPicuLoadedImageAdapter(getApplicationContext(), loadedImages, this);
        loadedImagesView = findViewById(R.id.loaded_images);
        loadedImagesView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        loadedImagesView.setAdapter(loadedImageAdapter);
        loadedImagesView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadImages();
                }
            }
        });

        selectedImages = new ArrayList<>();
        selectedImageAdapter = new KutuPicuSelectedImageAdapter(getApplicationContext(), selectedImages, this);
        selectedImagesView = findViewById(R.id.selected_images);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        selectedImagesView.setLayoutManager(linearLayoutManager);
        selectedImagesView.setAdapter(selectedImageAdapter);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(this).check();
    }

    private void parseIntentParams() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            maxSelection = bundle.getInt(CodeUtil.MAX_SELECTION, -1);
            minSelection = bundle.getInt(CodeUtil.MIN_SELECTION, -1);
        }
    }

    private void showTitle() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Choose Pictures");
            actionBar.setDisplayHomeAsUpEnabled(true);
            return;
        }

        androidx.appcompat.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Choose Pictures");
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        loadImages();
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {

    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

    }

    private void loadImages() {
        ArrayList<String> images = ImageUtil.readAllImages(this, page, limit);
        if (images.size() > 0) {
            page++;
        }

        Log.d("NumberOfImages", images.size() + "");

        for (String image : images) {
            this.loadedImages.add(new LoadedImage(image, false));
        }
        this.loadedImageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSelected(LoadedImage loadedImage) {
        if (maxSelection != -1 && loadedImage.isChecked() && selectedImages.size() >= maxSelection) {
            Toast.makeText(getApplicationContext(), "Maximum images selected", Toast.LENGTH_SHORT).show();
            onRemoved(new SelectedImage(loadedImage.getImage()));
            return;
        }

        if (loadedImage.isChecked()) {
            selectedImages.add(new SelectedImage(loadedImage.getImage()));
            selectedImageAdapter.notifyDataSetChanged();
        } else {
            SelectedImage foundImage = null;
            for (SelectedImage image : selectedImages) {
                if (image.getImage().equals(loadedImage.getImage())) {
                    foundImage = image;
                }
            }
            selectedImages.remove(foundImage);
            selectedImageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRemoved(SelectedImage selectedImage) {
        selectedImages.remove(selectedImage);
        selectedImageAdapter.notifyDataSetChanged();

        for (LoadedImage loadedImage : loadedImages) {
            if (selectedImage.getImage().equals(loadedImage.getImage())) {
                loadedImage.setChecked(false);
            }
        }
        loadedImageAdapter.notifyDataSetChanged();
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
        cameraMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(getApplicationContext(), CameraPickerActivity.class);
                startActivityForResult(i, CodeUtil.IMAGE_SELECTION_REQUEST_CODE);
                return false;
            }
        });

        MenuItem galleryMenuItem = menu.findItem(R.id.menu_item_switch_to_gallery);
        galleryMenuItem.setVisible(false);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode, data);
        finish();
    }

    private void onImageSelectionDone() {
        if (minSelection != -1 && selectedImages.size() < minSelection) {
            String msg = "Choose minimum " + minSelection + " images";
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            return;
        }

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

        loadedImages.clear();
        loadedImageAdapter.notifyDataSetChanged();

        page = 1;
        loadImages();
    }

    private void onImageSelectionBackPressed() {
        ArrayList<String> images = new ArrayList<>();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(CodeUtil.SELECTED_IMAGES_KEY, images);
        setResult(CodeUtil.IMAGE_SELECTION_BACK_PRESSED, resultIntent);
        finish();
    }
}
