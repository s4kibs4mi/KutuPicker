# KutuPicker [![](https://jitpack.io/v/ninja.sakib/KutuPicker.svg)](https://jitpack.io/#ninja.sakib/KutuPicker)
KutuPicker is a android image picker library using Camera & Gallery.

### Usages

1. Add in project level `build.gradle`

```groovy
allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven { url 'https://jitpack.io' }  // This Line
    }
}
```

2. Add in module level `build.gradle`

```groovy
dependencies {
	implementation 'ninja.sakib:KutuPicker:1.0.1'
}
```

3. Start Image Picker

* Using Gallery
```java
Intent i = new Intent(getApplicationContext(), GalleryPickerActivity.class);
i.putExtra(CodeUtil.MAX_SELECTION, 5);
i.putExtra(CodeUtil.MIN_SELECTION, 1);
startActivityForResult(i, CodeUtil.IMAGE_SELECTION_REQUEST_CODE);
```

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == CodeUtil.IMAGE_SELECTION_DONE) {
        ArrayList<String> selectedImages = data.getStringArrayListExtra(CodeUtil.SELECTED_IMAGES_KEY);
        for (String image : selectedImages) {
            Log.d("Selected", image);
        }
    }
}
```

* Using Camera
```java
Intent i = new Intent(getApplicationContext(), CameraPickerActivity.class);
i.putExtra(CodeUtil.MAX_SELECTION, 5);
i.putExtra(CodeUtil.MIN_SELECTION, 1);
startActivityForResult(i, CodeUtil.IMAGE_SELECTION_REQUEST_CODE);
```

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == CodeUtil.IMAGE_SELECTION_DONE) {
        ArrayList<String> selectedImages = data.getStringArrayListExtra(CodeUtil.SELECTED_IMAGES_KEY);
        for (String image : selectedImages) {
            Log.d("Selected", image);
        }
    }
}
```

### License
Copyright &copy;  Sakib Sami

Distributed under [MIT](https://github.com/s4kibs4mi/KutuPicker/blob/master/LICENSE) license
