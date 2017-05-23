package flyapp.its_on;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HLiu on 16/08/2014.
 */
public class ParcelHttpParams implements Parcelable {

    public String key;
    public String value;
    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(value);
    }

    public ParcelHttpParams(Parcel source){
        readFromParcel(source);
    }

    public ParcelHttpParams(){
    }

    public void readFromParcel(Parcel source){
        key = source.readString();
        value = source.readString();
    }

    public static final Parcelable.Creator<ParcelHttpParams> CREATOR =
            new Parcelable.Creator<ParcelHttpParams>() {

                @Override
                public ParcelHttpParams createFromParcel(Parcel source) {
                    return new ParcelHttpParams(source);
                }

                @Override
                public ParcelHttpParams[] newArray(int size) {
                    return new ParcelHttpParams[size];
                }
            };
}
