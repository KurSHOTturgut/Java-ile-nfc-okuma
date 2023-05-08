package com.ucuzpazargelarkadas.nfcmuhmmedkursatturgut;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "UserData", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create table Userdetalist(name TEXT primary key , contact TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop table if exists  Userdetalist");
    }

    public Boolean saveuserdata(String name , String contact){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name",name);
        contentValues.put("contact", contact);
        long result  = DB.insert("Userdetalist",null,contentValues);
        if (result == 1){
            return  false;
        }
        else {
            return  true;
        }
    }

    public Cursor gettext(){
        SQLiteDatabase DB  = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from Userdetalist",null);
        return  cursor;
    }

    public void deleteAllUserData() {
        SQLiteDatabase DB = this.getWritableDatabase();
        DB.delete("Userdetalist", null, null);
        DB.close();
    }
}