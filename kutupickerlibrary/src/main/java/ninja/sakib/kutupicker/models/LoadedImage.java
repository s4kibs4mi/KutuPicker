package ninja.sakib.kutupicker.models;

public class LoadedImage {
    private String image;
    private boolean isChecked;

    public LoadedImage() {

    }

    public LoadedImage(String image, boolean isChecked) {
        this.image = image;
        this.isChecked = isChecked;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
