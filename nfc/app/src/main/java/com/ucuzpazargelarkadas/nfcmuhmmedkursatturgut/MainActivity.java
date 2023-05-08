package com.ucuzpazargelarkadas.nfcmuhmmedkursatturgut;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;


public class MainActivity extends Activity {

    private NfcAdapter nfcAdapter;
    private TextView resultTextView;

    String tagIdString;
    String currentTimeString;
    String isim;
    DBHelper DB;

    Button Kydt , Gstr,sil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = (TextView) findViewById(R.id.result_text_view);
        Kydt= findViewById(R.id.save_button);
        Gstr=findViewById(R.id.show_button);
        sil=findViewById(R.id.sil);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Cihazınızda NFC bulunmuyor.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Lütfen NFC özelliğini açın.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleNfcIntent(intent);
    }


    private void handleNfcIntent(Intent intent) {
        DB = new DBHelper(this);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] tagId = tag.getId();
        tagIdString= ByteArrayToHexString(tagId);
        currentTimeString = DateFormat.getDateTimeInstance().format(new Date());
        resultTextView.setText("Kart ID'si: " + tagIdString + "\nOkunduğu Tarih: " + currentTimeString);
        Toast.makeText(this, "Kart Başarılı Bir Şekilde Okutuldu", Toast.LENGTH_SHORT).show();
        Kydt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameText= tagIdString.toString();
                String contactText = currentTimeString.toString();
                Boolean savedata = DB.saveuserdata(nameText,contactText);
                if(TextUtils.isEmpty(nameText)||TextUtils.isEmpty(contactText)){
                    Toast.makeText(MainActivity.this, "Kart id eklendi ve tarih", Toast.LENGTH_SHORT).show();

                    return;
                }
                else{
                    if(savedata==true){
                        Toast.makeText(MainActivity.this, "Bu Kart Daha önce Okutuldu", Toast.LENGTH_SHORT).show();

                    }
                    else{

                    }
                }
            }
        });
        Gstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor WL = DB.gettext();
                if(WL.getCount()==0){
                    Toast.makeText(MainActivity.this, "kart verileri mevcut değil", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    StringBuffer buffer = new StringBuffer();
                    while (WL.moveToNext()){
                        buffer.append("Kart_ID "+ WL.getString(0)+"\n");
                        buffer.append("okundu_Tarih: "+WL.getString(1)+"\n\n");
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("Okunan Kartlar");
                    builder.setMessage(buffer.toString());
                    builder.show();
                }
            }
        });

        sil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB.deleteAllUserData();
                Toast.makeText(MainActivity.this, "Kaydedilen Veriler Silindi", Toast.LENGTH_SHORT).show();
                resultTextView.setText("Tüm Kart Verileri Silindi");
            }
        });


    }
    private String readNameFromTag(Tag tag, int blockNumber) {
        MifareClassic mifareClassicTag = MifareClassic.get(tag);

        try {
            mifareClassicTag.connect();

            boolean auth = mifareClassicTag.authenticateSectorWithKeyA(mifareClassicTag.blockToSector(blockNumber), MifareClassic.KEY_DEFAULT);
            if (auth) {
                byte[] data = mifareClassicTag.readBlock(blockNumber);
                return new String(data, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mifareClassicTag != null) {
                try {
                    mifareClassicTag.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

//kart bilgisi


    public String readName(Tag tag) throws IOException {
        NfcA nfcA = NfcA.get(tag);

        if (nfcA != null) {
            nfcA.connect();
            byte[] readCmd = new byte[]{
                    (byte) 0x00, // CLA
                    (byte) 0xB0, // INS
                    (byte) 0x00, // P1
                    (byte) 0x0C, // P2 - İlk ad karakterinin adresi
                    (byte) 0x0E  // Lc - Adın uzunluğu (14 bayt)
            };
            byte[] response = nfcA.transceive(readCmd);
            if (response != null && response.length >= 50) {
                isim = new String(response, 0, 50, "UTF-8").trim();
                Toast.makeText(this, "ismim: "+isim, Toast.LENGTH_SHORT).show();
            }
            nfcA.close();

        }
        return isim;

    }



    private void enableForegroundDispatch() {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(
                this, pendingIntent, new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)},
                new String[][]{new String[]{NfcA.class.getName()}});

    }

    private void disableForegroundDispatch() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = new String[]{
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"
        };
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }
}
