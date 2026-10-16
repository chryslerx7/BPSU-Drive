package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Beginner-friendly SQLite database helper for BPSU Drive with database safety corrections.
// Features PBKDF2 salted password hashing, safe transactional upgrades, and foreign key integrity.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bpsu_drive.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_SERVICES = "service_types";
    public static final String TABLE_REQUESTS = "requests";
    public static final String TABLE_RECEIPTS = "receipts";

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
        // Create users table (stores student account info with secure password hash and salt)
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_STUDENT_ID + " TEXT UNIQUE NOT NULL, "
                + COLUMN_USER_NAME + " TEXT NOT NULL, "
                + COLUMN_USER_EMAIL + " TEXT NOT NULL, "
                + COLUMN_USER_PASSWORD_HASH + " TEXT NOT NULL, "
                + COLUMN_USER_COURSE + " TEXT NOT NULL" + ")";

        // Create service_types table (stores available campus services and fees)
        String CREATE_SERVICES_TABLE = "CREATE TABLE " + TABLE_SERVICES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SERVICE_NAME + " TEXT NOT NULL, "
                + COLUMN_SERVICE_FEE + " REAL NOT NULL, "
                + COLUMN_SERVICE_DESC + " TEXT NOT NULL" + ")";

        // Create requests table (stores student service requests linked to user and service)
        // Note: Request cancellation updates status to 'Cancelled'; records are preserved, not deleted.
        String CREATE_REQUESTS_TABLE = "CREATE TABLE " + TABLE_REQUESTS + "("
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

        // Create receipts table (stores payment receipt image paths and verification review status)
        String CREATE_RECEIPTS_TABLE = "CREATE TABLE " + TABLE_RECEIPTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_RECEIPT_REQUEST_ID + " INTEGER NOT NULL, "
                + COLUMN_RECEIPT_IMAGE_PATH + " TEXT NOT NULL, "
                + COLUMN_RECEIPT_STATUS + " TEXT NOT NULL, "
                + COLUMN_RECEIPT_SUBMITTED_AT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_RECEIPT_REQUEST_ID + ") REFERENCES " + TABLE_REQUESTS + "(" + COLUMN_ID + ") ON DELETE CASCADE" + ")";

        // Execute table creation queries
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_SERVICES_TABLE);
        db.execSQL(CREATE_REQUESTS_TABLE);
        db.execSQL(CREATE_RECEIPTS_TABLE);

        // Seed initial predefined campus service types
        seedServiceTypes(db);
    }

    // Safe incremental upgrade strategy to preserve existing user and request data
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            // Future schema migrations will be handled incrementally here (e.g., if (oldVersion < 2) { ... })
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Handle migration exception without dropping data tables
        } finally {
            db.endTransaction();
        }
    }

    // Helper method to seed default BPSU campus services
    private void seedServiceTypes(SQLiteDatabase db) {
        insertService(db, "Document Request (Transcript / Diploma)", 150.00, "Official academic records and certificate issuance.");
        insertService(db, "Clearance Processing", 50.00, "End-semester student clearance and approval signatures.");
        insertService(db, "Student ID Replacement", 200.00, "Replacement for lost or damaged official BPSU student identification cards.");
        insertService(db, "Certificate of Registration (COR)", 30.00, "Certified copy of official enrollment and subject registration.");
    }

    // Helper to insert a single service record during seeding
    private void insertService(SQLiteDatabase db, String name, double fee, String description) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SERVICE_NAME, name);
        values.put(COLUMN_SERVICE_FEE, fee);
        values.put(COLUMN_SERVICE_DESC, description);
        db.insert(TABLE_SERVICES, null, values);
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
            String saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP);
            String hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP);
            return iterations + ":" + saltBase64 + ":" + hashBase64;
        } catch (Exception e) {
            return null;
        }
    }

    // Secure password verification method for Phase 2 authentication
    public static boolean verifyPassword(String password, String storedPasswordHash) {
        try {
            String[] parts = storedPasswordHash.split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] storedHash = Base64.decode(parts[2], Base64.NO_WRAP);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, storedHash.length * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(storedHash, testHash);
        } catch (Exception e) {
            return false;
        }
    }
}
