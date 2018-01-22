package com.example.colin.audioprocessing;

/**
 * Created by Colin on 22/01/2018.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

//reference similar to the example dbmanager we were given
//reference for the raw queries here- https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html
public class DatabaseManager
{

    public static final String KEY_ROWID 	= "_id";
    public static final String KEY_NOTE	= "note";
    public static final String KEY_DURATION 	= "duration";
    private static final String DATABASE_NAME 	= "Transcriptions";
    private static final String DATABASE_TABLE 	= "Transcription_details";
    private static final int DATABASE_VERSION 	= 1;

    //
    private static final String DATABASE_CREATE = "create table "+ "Contact_Details" +
            "("+
            "_id integer primary key autoincrement" +
            ",note text not null" +
            ",duration text not null unique" +
            ",email text not null unique" +
            ",password text not null" +
            ",team text not null unique" +
            ");";

    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    //
    public DatabaseManager(Context ctx)
    {
        //
        this.context 	= ctx;
        DBHelper 		= new DatabaseHelper(context);
    }


    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        //
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        //
        public void onCreate(SQLiteDatabase db)
        {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        //
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            // dB structure change..

        }
    }   //

    public DatabaseManager open() throws SQLException
    {
        db     = DBHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        DBHelper.close();
    }
    //adds a person to the database
    public long insertPerson(String fullname, String username, String email,String password,String team)
    {
        db = DBHelper.getWritableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_FULLNAME, fullname);
        initialValues.put(KEY_USERNAME, username);
        initialValues.put(KEY_EMAIL, email);
        initialValues.put(KEY_PASSWORD, password);
        initialValues.put(KEY_TEAM, team);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //gets info or person with matching username and password entered
    //used for person log in
    public Cursor getPerson(String username,String password) throws SQLException
    {
        String query="SELECT * FROM " + DATABASE_TABLE +
                " WHERE " + KEY_USERNAME + "=\"" + username +
                "\" and " + KEY_PASSWORD + "=\"" + password + "\"";
        Cursor mCursor =
                db.rawQuery(query,null);

        if (mCursor != null)
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //gets a person with matching username and email and team
    //this is used to check if someone tries to register with already existing user data
    public Cursor checkReg(String username,String email,String team) throws SQLException
    {
        String query="SELECT * FROM " + DATABASE_TABLE +
                " WHERE " + KEY_USERNAME + "=\"" + username +
                "\" or " + KEY_EMAIL + "=\"" + email +
                "\" or " + KEY_TEAM + "=\"" + team + "\"";
        Cursor mCursor =
                db.rawQuery(query,null);

        if (mCursor != null)
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}//end class
//end references