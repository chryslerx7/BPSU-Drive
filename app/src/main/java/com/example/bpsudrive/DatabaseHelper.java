package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Beginner-friendly SQLite database helper for BPSU Drive with robust notifications migration and dynamic service catalog seeding.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "BpsuDriveDB";
    private static final String DATABASE_NAME = "bpsu_drive.db";
    private static final int DATABASE_VERSION = 4;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_SERVICES = "service_types";
    public static final String TABLE_REQUESTS = "requests";
    public static final String TABLE_RECEIPTS = "receipts";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    // Common column names
    public static final String COLUMN_ID = "id";

    // Users table columns
    public static final String COLUMN_USER_STUDENT_ID = "student_id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_USER_COURSE = "course";

    // Service types table columns
    public static final String COLUMN_SERVICE_NAME = "service_name";
    public static final String COLUMN_SERVICE_FEE = "fee";
    public static final String COLUMN_SERVICE_DESC = "description";

    // Requests table columns
    public static final String COLUMN_REQUEST_USER_ID = "user_id";
    public static final String COLUMN_REQUEST_SERVICE_ID = "service_id";
    public static final String COLUMN_REQUEST_REF_NO = "reference_no";
    public static final String COLUMN_REQUEST_PURPOSE = "purpose";
    public static final String COLUMN_REQUEST_ADDITIONAL = "additional_info";
    public static final String COLUMN_REQUEST_STATUS = "status";
    public static final String COLUMN_REQUEST_CREATED_AT = "created_at";

    // Receipts table columns
    public static final String COLUMN_RECEIPT_REQUEST_ID = "request_id";
    public static final String COLUMN_RECEIPT_IMAGE_PATH = "image_path";
    public static final String COLUMN_RECEIPT_STATUS = "verification_status";
    public static final String COLUMN_RECEIPT_SUBMITTED_AT = "submitted_at";

    // Notifications table columns
    public static final String COLUMN_NOTIF_USER_ID = "user_id";
    public static final String COLUMN_NOTIF_TITLE = "title";
    public static final String COLUMN_NOTIF_MESSAGE = "message";
    public static final String COLUMN_NOTIF_TIMESTAMP = "timestamp";
    public static final String COLUMN_NOTIF_IS_READ = "is_read";
    public static final String COLUMN_NOTIF_REQUEST_ID = "request_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Enable foreign key constraints on database connection
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate executing. Creating tables...");
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_STUDENT_ID + " TEXT UNIQUE NOT NULL, "
                + COLUMN_USER_NAME + " TEXT NOT NULL, "
                + COLUMN_USER_EMAIL + " TEXT NOT NULL, "
                + COLUMN_USER_PASSWORD_HASH + " TEXT NOT NULL, "
                + COLUMN_USER_COURSE + " TEXT NOT NULL" + ")";

        // Create service_types table
        String CREATE_SERVICES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SERVICES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SERVICE_NAME + " TEXT NOT NULL, "
                + COLUMN_SERVICE_FEE + " REAL NOT NULL, "
                + COLUMN_SERVICE_DESC + " TEXT NOT NULL" + ")";

        // Create requests table
        String CREATE_REQUESTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_REQUESTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_REQUEST_USER_ID + " INTEGER NOT NULL, "
                + COLUMN_REQUEST_SERVICE_ID + " INTEGER NOT NULL, "
                + COLUMN_REQUEST_REF_NO + " TEXT UNIQUE NOT NULL, "
                + COLUMN_REQUEST_PURPOSE + " TEXT NOT NULL, "
                + COLUMN_REQUEST_ADDITIONAL + " TEXT, "
                + COLUMN_REQUEST_STATUS + " TEXT NOT NULL, "
                + COLUMN_REQUEST_CREATED_AT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_REQUEST_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "), "
                + "FOREIGN KEY(" + COLUMN_REQUEST_SERVICE_ID + ") REFERENCES " + TABLE_SERVICES + "(" + COLUMN_ID + ")" + ")";

        // Create receipts table
        String CREATE_RECEIPTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_RECEIPTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_RECEIPT_REQUEST_ID + " INTEGER NOT NULL, "
                + COLUMN_RECEIPT_IMAGE_PATH + " TEXT NOT NULL, "
                + COLUMN_RECEIPT_STATUS + " TEXT NOT NULL, "
                + COLUMN_RECEIPT_SUBMITTED_AT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_RECEIPT_REQUEST_ID + ") REFERENCES " + TABLE_REQUESTS + "(" + COLUMN_ID + ") ON DELETE CASCADE" + ")";

        // Create notifications table
        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NOTIF_USER_ID + " INTEGER NOT NULL, "
                + COLUMN_NOTIF_TITLE + " TEXT NOT NULL, "
                + COLUMN_NOTIF_MESSAGE + " TEXT NOT NULL, "
                + COLUMN_NOTIF_TIMESTAMP + " TEXT NOT NULL, "
                + COLUMN_NOTIF_IS_READ + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_NOTIF_REQUEST_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_NOTIF_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY(" + COLUMN_NOTIF_REQUEST_ID + ") REFERENCES " + TABLE_REQUESTS + "(" + COLUMN_ID + ") ON DELETE SET NULL" + ")";

        // Execute table creation queries
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_SERVICES_TABLE);
        db.execSQL(CREATE_REQUESTS_TABLE);
        db.execSQL(CREATE_RECEIPTS_TABLE);
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
        Log.d(TAG, "All tables created successfully.");

        // Seed initial predefined campus service types
        checkAndSeedServices(db);
    }

    // Safe incremental upgrade strategy preserving existing data and ensuring all tables exist
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade executing from version " + oldVersion + " to " + newVersion);
        db.beginTransaction();
        try {
            String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_USER_STUDENT_ID + " TEXT UNIQUE NOT NULL, "
                    + COLUMN_USER_NAME + " TEXT NOT NULL, "
                    + COLUMN_USER_EMAIL + " TEXT NOT NULL, "
                    + COLUMN_USER_PASSWORD_HASH + " TEXT NOT NULL, "
                    + COLUMN_USER_COURSE + " TEXT NOT NULL" + ")";
            String CREATE_SERVICES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SERVICES + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_SERVICE_NAME + " TEXT NOT NULL, "
                    + COLUMN_SERVICE_FEE + " REAL NOT NULL, "
                    + COLUMN_SERVICE_DESC + " TEXT NOT NULL" + ")";
            String CREATE_REQUESTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_REQUESTS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_REQUEST_USER_ID + " INTEGER NOT NULL, "
                    + COLUMN_REQUEST_SERVICE_ID + " INTEGER NOT NULL, "
                    + COLUMN_REQUEST_REF_NO + " TEXT UNIQUE NOT NULL, "
                    + COLUMN_REQUEST_PURPOSE + " TEXT NOT NULL, "
                    + COLUMN_REQUEST_ADDITIONAL + " TEXT, "
                    + COLUMN_REQUEST_STATUS + " TEXT NOT NULL, "
                    + COLUMN_REQUEST_CREATED_AT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + COLUMN_REQUEST_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "), "
                    + "FOREIGN KEY(" + COLUMN_REQUEST_SERVICE_ID + ") REFERENCES " + TABLE_SERVICES + "(" + COLUMN_ID + ")" + ")";
            String CREATE_RECEIPTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_RECEIPTS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_RECEIPT_REQUEST_ID + " INTEGER NOT NULL, "
                    + COLUMN_RECEIPT_IMAGE_PATH + " TEXT NOT NULL, "
                    + COLUMN_RECEIPT_STATUS + " TEXT NOT NULL, "
                    + COLUMN_RECEIPT_SUBMITTED_AT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + COLUMN_RECEIPT_REQUEST_ID + ") REFERENCES " + TABLE_REQUESTS + "(" + COLUMN_ID + ") ON DELETE CASCADE" + ")";
            String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NOTIF_USER_ID + " INTEGER NOT NULL, "
                    + COLUMN_NOTIF_TITLE + " TEXT NOT NULL, "
                    + COLUMN_NOTIF_MESSAGE + " TEXT NOT NULL, "
                    + COLUMN_NOTIF_TIMESTAMP + " TEXT NOT NULL, "
                    + COLUMN_NOTIF_IS_READ + " INTEGER NOT NULL DEFAULT 0, "
                    + COLUMN_NOTIF_REQUEST_ID + " INTEGER, "
                    + "FOREIGN KEY(" + COLUMN_NOTIF_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY(" + COLUMN_NOTIF_REQUEST_ID + ") REFERENCES " + TABLE_REQUESTS + "(" + COLUMN_ID + ") ON DELETE SET NULL" + ")";

            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_SERVICES_TABLE);
            db.execSQL(CREATE_REQUESTS_TABLE);
            db.execSQL(CREATE_RECEIPTS_TABLE);
            db.execSQL(CREATE_NOTIFICATIONS_TABLE);

            checkAndSeedServices(db);

            db.setTransactionSuccessful();
            Log.d(TAG, "onUpgrade completed successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error during onUpgrade", e);
        } finally {
            db.endTransaction();
        }
    }

    // Ensures default services exist in SQLite without duplicating existing records
    public void checkAndSeedServices(SQLiteDatabase db) {
        insertServiceIfNotExists(db, "Document Request (Transcript / Diploma)", 150.00, "Official academic records and certificate issuance.");
        insertServiceIfNotExists(db, "Clearance Processing", 50.00, "End-semester student clearance and approval signatures.");
        insertServiceIfNotExists(db, "Student ID Replacement", 200.00, "Replacement for lost or damaged official BPSU student identification cards.");
        insertServiceIfNotExists(db, "Certificate of Registration (COR)", 10.00, "Certified copy of official enrollment and subject registration.");
        insertServiceIfNotExists(db, "Transcript of Record (TOR)", 20.00, "Certified copy of official Transcript of Record.");
        insertServiceIfNotExists(db, "Library Card Replacement", 50.00, "Replacement for lost or damaged official BPSU student library cards.");
    }

    private void insertServiceIfNotExists(SQLiteDatabase db, String name, double fee, String description) {
        try (Cursor cursor = db.query(TABLE_SERVICES, new String[]{COLUMN_ID},
                COLUMN_SERVICE_NAME + " = ?", new String[]{name}, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_SERVICE_NAME, name);
                values.put(COLUMN_SERVICE_FEE, fee);
                values.put(COLUMN_SERVICE_DESC, description);
                db.insert(TABLE_SERVICES, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting service " + name, e);
        }
    }

    // Validates password requirements: 8+ chars, 1 uppercase, 1 lowercase, 1 digit, 1 special character
    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // Secure password hashing using PBKDF2 with HMAC-SHA-256 and a unique random salt
    public static String hashPassword(String password) {
        try {
            int iterations = 10000;
            int keyLength = 256;
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Format: iterations:base64(salt):base64(hash)
            return iterations + ":" + Base64.encodeToString(salt, Base64.NO_WRAP) + ":" + Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Error hashing password", e);
            return null;
        }
    }

    // Constant-time password verification against stored PBKDF2 hash
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] expectedHash = Base64.decode(parts[2], Base64.NO_WRAP);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, expectedHash.length * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            Log.e(TAG, "Error verifying password", e);
            return false;
        }
    }
}
