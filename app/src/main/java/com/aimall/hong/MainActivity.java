package com.aimall.hong;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.image);
        TextView tvStringTest = findViewById(R.id.tv_string_test);

        byte[] imageByte = getFromRaw(R.raw.card_buhuo);
        imageByte = AESUtils.decrypt(imageByte, BuildConfig.AES_KEY);

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }

        byte[] stringTest = getFromRaw(R.raw.string);
        stringTest = AESUtils.decrypt(stringTest, BuildConfig.AES_KEY);
        if (stringTest!=null){
            String string = new String(stringTest);
            tvStringTest.setText(string);
        }

    }

    public byte[] getFromRaw(int rawRes) {
        try {
            InputStream in = getResources().openRawResource(rawRes);
            //获取文件的字节数
            int length = in.available();
            //创建byte数组
            byte[] buffer = new byte[length];
            //将文件中的数据读到byte数组中
            in.read(buffer);
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
