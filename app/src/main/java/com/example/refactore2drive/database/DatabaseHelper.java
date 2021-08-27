package com.example.refactore2drive.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.refactore2drive.chart.Value;
import com.example.refactore2drive.models.Account;
import com.example.refactore2drive.models.Contact;
import com.example.refactore2drive.models.Device;
import com.example.refactore2drive.models.Discapacity;
import com.example.refactore2drive.models.Disease;
import com.example.refactore2drive.models.Person;
import com.example.refactore2drive.models.SessionModel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG =" DatabaseHelper";

    private static final int DATABASE_VERSION = 1;

    private static final  String DATABASE_NAME = "e2Drive";

    //Table Names
    private static final String TABLE_ACCOUNT="Account";
    private static final String TABLE_CONTACT="Contact";
    private static final String TABLE_PERSON="Person";
    private static final String TABLE_DISCAPACITY="Discapacity";
    private static final String TABLE_INJURY="Injury";
    private static final String TABLE_OBD="Obd_Device";
    private static final String TABLE_WEAR="Wear_Device";
    private static final String TABLE_SESSION="Session";
    private static final String TABLE_DATA_SPEED="Valocidad";
    private static final String TABLE_DATA_CONSUME="Consumo";
    private static final String TABLE_DATA_HEART="Pulso";
    private static final String COLUMN_ID="id";

    //Account columns
    private static final String COLUMN_ACCOUNT_NICKNAME="nickname";
    private static final String COLUMN_ACCOUNT_PASSWORD="password";

    //Contact columns
    private static final String COLUMN_CONTACT_NAME="name";
    private static final String COLUMN_CONTACT_TELEPHONE="number";
    private static final String COLUMN_CONTACT_NICKNAME="nickname";

    //Person columns
    private static final String COLUMN_PERSON_NAME = "name";
    private static final String COLUMN_PERSON_NICKNAME = "nickname";
    private static final String COLUMN_PERSON_AGE = "age";
    private static final String COLUMN_PERSON_GENRE = "genre";
    private static final String COLUMN_PERSON_WEIGHT = "weight";
    private static final String COLUMN_PERSON_HEIGHT = "height";

    //Discapacity columns
    private static final String COLUMN_DISCAPACITY_TYPE = "type";
    private static final String COLUMN_DISCAPACITY_DEGREE = "degree";
    private static final String COLUMN_DISCAPACITY_NICKNAME = "nickname";

    //Injury columns
    private static final String COLUMN_INJURY_NAME="name";
    private static final String COLUMN_INJURY_NICKNAME="nickname";

    //OBD columns
    private static final String COLUMN_OBD_NAME="name";
    private static final String COLUMN_OBD_ADDRESS="device";
    private static final String COLUMN_OBD_NICKNAME="nickname";

    //WEAR columns
    private static final String COLUMN_WEAR_NAME="name";
    private static final String COLUMN_WEAR_ADDRESS="device";
    private static final String COLUMN_WEAR_NICKNAME="nickname";

    //Session columns
    private static final String COLUMN_SESSION_NAME="name";
    private static final String COLUMN_SESSION_COMMENTS="comments";
    private static final String COLUMN_SESSION_USERNAME="username";
    private static final String COLUMN_SESSION_TINI="tIni";
    private static final String COLUMN_SESSION_TFIN="tFin";

    //Data record columns
    private static final String COLUMN_DATA_TIME="time";
    private static final String COLUMN_DATA_DATE="date";
    private static final String COLUMN_DATA_VALUE="value";
    private static final String COLUMN_DATA_NICKNAME="nickname";

    //Creating tables
    private static final String SQL_CREATE_ACCOUNT =
            "CREATE TABLE " + TABLE_ACCOUNT + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_ACCOUNT_NICKNAME + " TEXT UNIQUE," +
                    COLUMN_ACCOUNT_PASSWORD + " TEXT NOT NULL, " +
                    "FOREIGN KEY("+ COLUMN_ACCOUNT_NICKNAME+") REFERENCES " + TABLE_PERSON +"("+ COLUMN_PERSON_NICKNAME+")" +
                    ")";
    private static final String SQL_CRATE_CONTACT =
            "CREATE TABLE " + TABLE_CONTACT + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_CONTACT_NAME + " TEXT NOT NULL," +
                    COLUMN_CONTACT_TELEPHONE + " NUMBER NOT NULL," +
                    COLUMN_CONTACT_NICKNAME + " TEXT," +
                    "FOREIGN KEY(" + COLUMN_PERSON_NICKNAME+") REFERENCES " + TABLE_PERSON +"("+ COLUMN_PERSON_NICKNAME+")" +
                    ")";
    private static final String SQL_CREATE_DISCAPACITY =
            "CREATE TABLE " + TABLE_DISCAPACITY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_DISCAPACITY_TYPE + " TEXT NOT NULL," +
                    COLUMN_DISCAPACITY_DEGREE + " NUMBER NOT NULL," +
                    COLUMN_DISCAPACITY_NICKNAME + " TEXT UNIQUE," +
                    "FOREIGN KEY(" + COLUMN_PERSON_NICKNAME + ") REFERENCES " + TABLE_PERSON + "(" + COLUMN_PERSON_NICKNAME+")" +
                    ")";
    private static final String SQL_CREATE_INJURY =
            "CREATE TABLE " + TABLE_INJURY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_INJURY_NAME + " TEXT NOT NULL," +
                    COLUMN_INJURY_NICKNAME + " TEXT," +
                    "FOREIGN KEY(" + COLUMN_INJURY_NICKNAME+") REFERENCES " + TABLE_PERSON +"("+ COLUMN_PERSON_NICKNAME+")" +
                    ")";
    private static final String SQL_CREATE_PERSON =
            "CREATE TABLE " + TABLE_PERSON +" (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_PERSON_NAME + " TEXT NOT NULL," +
                    COLUMN_PERSON_NICKNAME + " TEXT UNIQUE NOT NULL," +
                    COLUMN_PERSON_AGE + " NUMBER NOT NULL," +
                    COLUMN_PERSON_GENRE + " TEXT NOT NULL," +
                    COLUMN_PERSON_HEIGHT + " NUMBER NOT NULL," +
                    COLUMN_PERSON_WEIGHT + " NUMBER)";
    private static final String SQL_CREATE_ODB =
            "CREATE TABLE " + TABLE_OBD + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_OBD_NAME + " TEXT NOT NULL," +
                    COLUMN_OBD_ADDRESS + " TEXT NOT NULL,"+
                    COLUMN_OBD_NICKNAME + " TEXT UNIQUE NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_OBD_NICKNAME + ") REFERENCES " + TABLE_PERSON + "(" + COLUMN_PERSON_NICKNAME + ")" +
                    ")";
    private static final String SQL_CREATE_WEAR =
            "CREATE TABLE " + TABLE_WEAR + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_WEAR_NAME + " TEXT NOT NULL," +
                    COLUMN_WEAR_ADDRESS + " TEXT NOT NULL,"+
                    COLUMN_WEAR_NICKNAME + " TEXT UNIQUE NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_WEAR_NICKNAME + ") REFERENCES " + TABLE_PERSON + "(" + COLUMN_PERSON_NICKNAME + ")" +
                    ")";
    private static final String SQL_CREATE_SESSION =
            "CREATE TABLE " + TABLE_SESSION + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_SESSION_TINI + " TEXT NOT NULL," +
                    COLUMN_SESSION_TFIN + " TEXT NOT NULL," +
                    COLUMN_SESSION_COMMENTS + " TEXT," +
                    COLUMN_SESSION_NAME + " TEXT NOT NULL UNIQUE," +
                    COLUMN_SESSION_USERNAME + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_SESSION_USERNAME + ") REFERENCES " + TABLE_PERSON + "(" + COLUMN_PERSON_NICKNAME +")" +
                    ")";
    private static final String SQL_CREATE_DATA_HEART =
            "CREATE TABLE " + TABLE_DATA_HEART + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_DATA_TIME + " NUMBER NOT NULL," +
                    COLUMN_DATA_DATE + " TEXT NOT NULL," +
                    COLUMN_DATA_VALUE + " NUMBER NOT NULL," +
                    COLUMN_DATA_NICKNAME + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_DATA_NICKNAME + ") REFERENCES " + TABLE_PERSON + "(" + COLUMN_PERSON_NICKNAME + ")" +
                    ")";
    private static final String SQL_CREATE_DATA_SPEED =
            "CREATE TABLE " + TABLE_DATA_SPEED + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_DATA_TIME + " NUMBER NOT NULL," +
                    COLUMN_DATA_DATE + " TEXT NOT NULL," +
                    COLUMN_DATA_VALUE + " NUMBER NOT NULL," +
                    COLUMN_DATA_NICKNAME + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_DATA_NICKNAME + ") REFERENCES " + TABLE_PERSON + "(" + COLUMN_PERSON_NICKNAME + ")" +
                    ")";
    private static final String SQL_CREATE_DATA_CONSUME =
            "CREATE TABLE " + TABLE_DATA_CONSUME + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_DATA_TIME + " NUMBER NOT NULL," +
                    COLUMN_DATA_DATE + " TEXT NOT NULL," +
                    COLUMN_DATA_VALUE + " NUMBER NOT NULL," +
                    COLUMN_DATA_NICKNAME + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_DATA_NICKNAME + ") REFERENCES " + TABLE_PERSON + "(" + COLUMN_PERSON_NICKNAME + ")" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PERSON);
        db.execSQL(SQL_CRATE_CONTACT);
        db.execSQL(SQL_CREATE_ACCOUNT);
        db.execSQL(SQL_CREATE_DISCAPACITY);
        db.execSQL(SQL_CREATE_INJURY);
        db.execSQL(SQL_CREATE_ODB);
        db.execSQL(SQL_CREATE_WEAR);
        db.execSQL(SQL_CREATE_SESSION);
        db.execSQL(SQL_CREATE_DATA_HEART);
        db.execSQL(SQL_CREATE_DATA_CONSUME);
        db.execSQL(SQL_CREATE_DATA_SPEED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INJURY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISCAPACITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEAR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA_SPEED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA_HEART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA_CONSUME);
        onCreate(db);
    }

    public void initDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_CREATE_PERSON);
        db.execSQL(SQL_CRATE_CONTACT);
        db.execSQL(SQL_CREATE_ACCOUNT);
        db.execSQL(SQL_CREATE_DISCAPACITY);
        db.execSQL(SQL_CREATE_INJURY);
        db.execSQL(SQL_CREATE_ODB);
        db.execSQL(SQL_CREATE_WEAR);
        db.execSQL(SQL_CREATE_SESSION);
        db.execSQL(SQL_CREATE_DATA_HEART);
        db.execSQL(SQL_CREATE_DATA_CONSUME);
        db.execSQL(SQL_CREATE_DATA_SPEED);
    }

    public void clearDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INJURY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISCAPACITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEAR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA_SPEED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA_HEART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA_CONSUME);
    }
    public void createPerson(Person person) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PERSON_NAME, person.getName());
        cv.put(COLUMN_PERSON_AGE, person.getAge());
        cv.put(COLUMN_PERSON_GENRE, person.getGenre());
        cv.put(COLUMN_PERSON_NICKNAME, person.getNickname());
        cv.put(COLUMN_PERSON_WEIGHT, person.getWeight());
        cv.put(COLUMN_PERSON_HEIGHT, person.getHeight());
        db.insert(TABLE_PERSON,null,cv);
    }

    public void createInjury(Disease injury) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_INJURY_NAME, injury.getName());
        cv.put(COLUMN_INJURY_NICKNAME, injury.getNickname());
        db.insert(TABLE_INJURY, null,cv);
    }

    public void createDiscapacity(Discapacity discapacity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DISCAPACITY_NICKNAME, discapacity.getNickname());
        cv.put(COLUMN_DISCAPACITY_DEGREE, discapacity.getDegree());
        cv.put(COLUMN_DISCAPACITY_TYPE, discapacity.getType());
        db.insert(TABLE_DISCAPACITY, null, cv);
    }

    public void createContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CONTACT_NAME, contact.getName());
        cv.put(COLUMN_CONTACT_NICKNAME, contact.getNickname());
        cv.put(COLUMN_CONTACT_TELEPHONE, contact.getNumber());
        db.insert(TABLE_CONTACT, null, cv);
    }

    public void createAccount(Account account) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ACCOUNT_NICKNAME, account.getName());
        cv.put(COLUMN_ACCOUNT_PASSWORD, account.getPassword());
        db.insert(TABLE_ACCOUNT, null, cv);
    }

    public void createObd(Device device) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_OBD_ADDRESS, device.getAddress());
        cv.put(COLUMN_OBD_NAME, device.getName());
        cv.put(COLUMN_OBD_NICKNAME, device.getNickname());
        db.insert(TABLE_OBD, null, cv);
    }

    public void createWear(Device device) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WEAR_ADDRESS, device.getAddress());
        cv.put(COLUMN_WEAR_NAME, device.getName());
        cv.put(COLUMN_WEAR_NICKNAME, device.getNickname());
        db.insert(TABLE_WEAR, null, cv);
    }

    public void createSession(SessionModel sessionModel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SESSION_NAME, sessionModel.getName());
        cv.put(COLUMN_SESSION_COMMENTS, sessionModel.getComments());
        cv.put(COLUMN_SESSION_TFIN, sessionModel.gettFin());
        cv.put(COLUMN_SESSION_TINI, sessionModel.gettIni());
        cv.put(COLUMN_SESSION_USERNAME, sessionModel.getUsername());
        db.insert(TABLE_SESSION, null, cv);
    }

    public void createDataConsume(Value value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DATA_NICKNAME, value.getNickname());
        cv.put(COLUMN_DATA_VALUE, value.getY());
        cv.put(COLUMN_DATA_TIME, value.getX());
        cv.put(COLUMN_DATA_DATE, value.getDate());
        db.insert(TABLE_DATA_CONSUME, null, cv);
    }

    public void createDataSpeed(Value value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DATA_NICKNAME, value.getNickname());
        cv.put(COLUMN_DATA_VALUE, value.getY());
        cv.put(COLUMN_DATA_TIME, value.getX());
        cv.put(COLUMN_DATA_DATE, value.getDate());
        db.insert(TABLE_DATA_SPEED, null, cv);
    }

    public long createDataHeart(Value value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DATA_NICKNAME, value.getNickname());
        cv.put(COLUMN_DATA_VALUE, value.getY());
        cv.put(COLUMN_DATA_TIME, value.getX());
        cv.put(COLUMN_DATA_DATE, value.getDate());
        return db.insert(TABLE_DATA_HEART, null, cv);
    }

    @SuppressLint("Range")
    public Person getPerson(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PERSON + " WHERE " + COLUMN_ID + " = " + id;
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) cursor.moveToFirst();
        Person person = new Person();
        if (cursor != null) {
            person.setAge(cursor.getInt(cursor.getColumnIndex(COLUMN_PERSON_AGE)));
            person.setGenre(cursor.getString(cursor.getColumnIndex(COLUMN_PERSON_GENRE)));
            person.setHeight(cursor.getInt(cursor.getColumnIndex(COLUMN_PERSON_WEIGHT)));
            person.setName(cursor.getString(cursor.getColumnIndex(COLUMN_PERSON_NAME)));
            person.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_PERSON_NICKNAME)));
            person.setWeight(cursor.getInt(cursor.getColumnIndex(COLUMN_PERSON_WEIGHT)));
            person.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            cursor.close();
        }
        return person;
    }

    @SuppressLint("Range")
    public Person getPerson(String nickname) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PERSON + " WHERE " + COLUMN_PERSON_NICKNAME + " = " + "'" +nickname +"'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) cursor.moveToFirst();
        Person person = new Person();
        if (cursor != null) {
            person.setAge(cursor.getInt(cursor.getColumnIndex(COLUMN_PERSON_AGE)));
            person.setGenre(cursor.getString(cursor.getColumnIndex(COLUMN_PERSON_GENRE)));
            person.setHeight(cursor.getInt(cursor.getColumnIndex(COLUMN_PERSON_WEIGHT)));
            person.setName(cursor.getString(cursor.getColumnIndex(COLUMN_PERSON_NAME)));
            person.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_PERSON_NICKNAME)));
            person.setWeight(cursor.getInt(cursor.getColumnIndex(COLUMN_PERSON_WEIGHT)));
            person.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            cursor.close();
        }
        return person;
    }

    @SuppressLint("Range")
    public List<Disease> getInjuries(String nickname) {
        ArrayList<Disease> injuries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_INJURY + " WHERE " + COLUMN_INJURY_NICKNAME + " = '" + nickname + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Disease injury = new Disease();
                injury.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                injury.setName(cursor.getString(cursor.getColumnIndex(COLUMN_INJURY_NAME)));
                injury.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_INJURY_NICKNAME)));
                injuries.add(injury);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return injuries;
    }

    @SuppressLint("Range")
    public List<Contact> getContacts(String nickname) {
        ArrayList<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CONTACT + " WHERE " + COLUMN_CONTACT_NICKNAME + " = '" + nickname + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_NAME)));
                contact.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_INJURY_NICKNAME)));
                contact.setNumber(cursor.getInt(cursor.getColumnIndex(COLUMN_CONTACT_TELEPHONE)));
                contacts.add(contact);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return contacts;
    }

    @SuppressLint("Range")
    public List<Discapacity> getDiscapacity(String nickname) {
        ArrayList<Discapacity> discapacities = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DISCAPACITY + " WHERE " + COLUMN_DISCAPACITY_NICKNAME + " = '" + nickname +"'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Discapacity discapacity = new Discapacity();
                discapacity.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                discapacity.setDegree(cursor.getInt(cursor.getColumnIndex(COLUMN_DISCAPACITY_DEGREE)));
                discapacity.setType(cursor.getString(cursor.getColumnIndex(COLUMN_DISCAPACITY_TYPE)));
                discapacity.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_DISCAPACITY_NICKNAME)));
                discapacities.add(discapacity);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return discapacities;
    }

    public List<String> getPersonNickname(String nickname) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_PERSON_NICKNAME + " FROM " + TABLE_PERSON + " WHERE " + COLUMN_PERSON_NICKNAME + " = '" + nickname +"'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String nick = cursor.getString(cursor.getColumnIndex(COLUMN_DISCAPACITY_NICKNAME));
                list.add(nick);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public Account getAccount(String nickname) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ACCOUNT + " WHERE " + COLUMN_ACCOUNT_NICKNAME + " = '" + nickname + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) cursor.moveToFirst();
        Account account = new Account();
        if (cursor != null) {
            account.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            account.setName(cursor.getString(cursor.getColumnIndex(COLUMN_ACCOUNT_NICKNAME)));
            account.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_ACCOUNT_PASSWORD)));
            cursor.close();
        }
        return account;
    }

    @SuppressLint("Range")
    public Device getObd(String nickname) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_OBD + " WHERE " + COLUMN_OBD_NICKNAME + " = '" + nickname + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) cursor.moveToFirst();
        Device device = new Device();
        if (cursor != null) {
            device.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            device.setAddress(cursor.getString(cursor.getColumnIndex(COLUMN_OBD_ADDRESS)));
            device.setName(cursor.getString(cursor.getColumnIndex(COLUMN_OBD_NAME)));
            device.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_OBD_NICKNAME)));
            cursor.close();
        }
        return device;
    }

    @SuppressLint("Range")
    public Device getWear(String nickname) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_WEAR + " WHERE " + COLUMN_WEAR_NICKNAME + " = '" + nickname + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) cursor.moveToFirst();
        Device device = new Device();
        if (cursor != null) {
            device.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            device.setAddress(cursor.getString(cursor.getColumnIndex(COLUMN_WEAR_ADDRESS)));
            device.setName(cursor.getString(cursor.getColumnIndex(COLUMN_WEAR_NAME)));
            device.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_WEAR_NICKNAME)));
            cursor.close();
        }
        return device;
    }

    @SuppressLint("Range")
    public List<SessionModel> getSessions(String nickname) {
        ArrayList<SessionModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SESSION + " WHERE " + COLUMN_SESSION_USERNAME + " = '" + nickname + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                SessionModel sessionModel = new SessionModel();
                sessionModel.setComments(cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_COMMENTS)));
                sessionModel.setName(cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_NAME)));
                sessionModel.settIni(cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_TINI)));
                sessionModel.settFin(cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_TFIN)));
                sessionModel.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                list.add(sessionModel);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<Value> getDataSpeed(String nickname) {
        ArrayList<Value> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DATA_SPEED + " WHERE " + COLUMN_DATA_NICKNAME + " = '" + nickname + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Value value = new Value();
                value.setX(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_TIME)));
                value.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_DATE)));
                value.setY(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_VALUE)));
                value.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_NICKNAME)));
                value.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                list.add(value);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<Value> getDataHeart(String nickname) {
        ArrayList<Value> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DATA_HEART + " WHERE " + COLUMN_DATA_NICKNAME + " = '" + nickname + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Value value = new Value();
                value.setX(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_TIME)));
                value.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_DATE)));
                value.setY(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_VALUE)));
                value.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_NICKNAME)));
                value.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                list.add(value);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<Value> getDataConsume(String nickname) {
        ArrayList<Value> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DATA_CONSUME + " WHERE " + COLUMN_DATA_NICKNAME + " = '" + nickname + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Value value = new Value();
                value.setX(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_TIME)));
                value.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_DATE)));
                value.setY(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_VALUE)));
                value.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_NICKNAME)));
                value.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                list.add(value);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<Value> getDataSpeedByDate(String nickname, String date) {
        ArrayList<Value> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DATA_SPEED + " WHERE " + COLUMN_DATA_NICKNAME + " = '" + nickname + "' AND " + COLUMN_DATA_DATE + " = '" + date + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Value value = new Value();
                value.setX(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_TIME)));
                value.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_DATE)));
                value.setY(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_VALUE)));
                value.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_NICKNAME)));
                value.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                list.add(value);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<Value> getDataHeartByDate(String nickname, String date) {
        ArrayList<Value> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DATA_HEART + " WHERE " + COLUMN_DATA_NICKNAME + " = '" + nickname + "' AND " + COLUMN_DATA_DATE + " = '" + date + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Value value = new Value();
                value.setX(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_TIME)));
                value.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_DATE)));
                value.setY(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_VALUE)));
                value.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_NICKNAME)));
                value.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                list.add(value);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<Value> getDataConsumeByDate(String nickname, String date) {
        ArrayList<Value> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DATA_CONSUME + " WHERE " + COLUMN_DATA_NICKNAME + " = '" + nickname + "' AND " + COLUMN_DATA_DATE + " = '" + date + "'";
        Log.e(LOG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Value value = new Value();
                value.setX(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_TIME)));
                value.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_DATE)));
                value.setY(cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_VALUE)));
                value.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_NICKNAME)));
                value.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                list.add(value);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int updatePerson(Person person) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PERSON_HEIGHT, person.getHeight());
        cv.put(COLUMN_PERSON_WEIGHT, person.getWeight());
        cv.put(COLUMN_PERSON_NICKNAME, person.getNickname());
        cv.put(COLUMN_PERSON_GENRE, person.getGenre());
        cv.put(COLUMN_PERSON_AGE, person.getAge());
        cv.put(COLUMN_PERSON_NAME, person.getName());

        return db.update(TABLE_PERSON, cv, COLUMN_ID + " = ?",
                new String[] {String.valueOf(person.getId())});
    }

    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CONTACT_NAME, contact.getName());
        cv.put(COLUMN_CONTACT_NICKNAME, contact.getNickname());
        cv.put(COLUMN_CONTACT_TELEPHONE, contact.getNumber());
        return db.update(TABLE_CONTACT, cv, COLUMN_ID + " = ?",
                new String[] {String.valueOf(contact.getId())});
    }

    public int updateDiscapacity(Discapacity discapacity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DISCAPACITY_TYPE, discapacity.getType());
        cv.put(COLUMN_DISCAPACITY_DEGREE, discapacity.getDegree());
        cv.put(COLUMN_DISCAPACITY_NICKNAME, discapacity.getNickname());
        return db.update(TABLE_DISCAPACITY, cv, COLUMN_ID + " = ?",
                new String[] {String.valueOf(discapacity.getId())});
    }

    public int updateInjury(Disease injury) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_INJURY_NICKNAME, injury.getNickname());
        cv.put(COLUMN_INJURY_NAME, injury.getName());
        return db.update(TABLE_INJURY, cv, COLUMN_ID + " = ?",
                new String[] {String.valueOf(injury.getId())});
    }

    public int updateAccount(Account account) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ACCOUNT_NICKNAME, account.getName());
        cv.put(COLUMN_ACCOUNT_PASSWORD, account.getPassword());
        return db.update(TABLE_ACCOUNT, cv, COLUMN_ID + " = ?",
                new String[] {String.valueOf(account.getId())});
    }

    public int updateObd(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_OBD_NICKNAME, device.getNickname());
        cv.put(COLUMN_OBD_NAME, device.getName());
        cv.put(COLUMN_OBD_ADDRESS, device.getAddress());
        return db.update(TABLE_OBD, cv, COLUMN_ID + " = ?",
                new String[] {String.valueOf(device.getId())});
    }

    public int updateWear(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WEAR_NICKNAME, device.getNickname());
        cv.put(COLUMN_WEAR_NAME, device.getName());
        cv.put(COLUMN_WEAR_ADDRESS, device.getAddress());
        return db.update(TABLE_WEAR, cv, COLUMN_ID + " = ?",
                new String[] {String.valueOf(device.getId())});
    }

    public void deletePerson(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PERSON, COLUMN_ID + " = ?",
                new String[] {String.valueOf(id)});
    }

    public void deleteAccount(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACCOUNT, COLUMN_ID + " = ?",
                new String[] {String.valueOf(id)});
    }

    public void deleteInjury(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INJURY, COLUMN_INJURY_NICKNAME + " = ?", new String[]{username});
    }

    public void deleteDiscapacity(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DISCAPACITY, COLUMN_ID + " = ?",
                new String[] {String.valueOf(id)});
    }

    public void deleteContact(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACT, COLUMN_ID + " = ?",
                new String[] {String.valueOf(id)});
    }

    public void deleteWear(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WEAR, COLUMN_ID + " = ?",
                new String[] {String.valueOf(id)});
    }

    public void deleteObd(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OBD, COLUMN_ID + " = ?",
                new String[] {String.valueOf(id)});
    }

    public void deleteSession(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SESSION, COLUMN_ID + " = ?",
                new String[] {String.valueOf(id)});
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) db.close();
    }
}
