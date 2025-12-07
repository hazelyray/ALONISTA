package com.enrollment.system.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utility to update database schema when new enum values are added.
 * This is particularly useful for SQLite which doesn't automatically update CHECK constraints.
 */
@Component
public class DatabaseSchemaUpdater {
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    /**
     * Updates the users table to allow TEACHER role.
     * This method drops and recreates the role column's CHECK constraint to include TEACHER.
     */
    public void updateUsersTableForTeacherRole() {
        if (dataSource == null) {
            System.out.println("⚠ DataSource not available, skipping schema update");
            return;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            String dbUrl = conn.getMetaData().getURL();
            
            // Only proceed if it's SQLite
            if (!dbUrl.contains("sqlite")) {
                System.out.println("⚠ Not a SQLite database, skipping schema update");
                return;
            }
            
            // Check if TEACHER role constraint needs to be added
            if (needsTeacherRoleUpdate(conn)) {
                System.out.println("🔄 Updating users table schema to support TEACHER role...");
                updateRoleConstraint(conn);
                System.out.println("✅ Users table schema updated successfully");
            } else {
                System.out.println("✓ Users table already supports TEACHER role");
            }
            
        } catch (Exception e) {
            System.err.println("⚠ Error updating database schema: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - allow application to continue
        }
    }
    
    private boolean needsTeacherRoleUpdate(Connection conn) throws Exception {
        // Try to insert a test value to see if TEACHER is allowed
        // This is a simple check - if the constraint exists and doesn't include TEACHER, we need to update
        try (Statement stmt = conn.createStatement()) {
            // Check if we can query the table structure
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "users", "role");
            
            if (columns.next()) {
                // Column exists, now check constraint
                // SQLite stores CHECK constraints in sqlite_master
                ResultSet tables = stmt.executeQuery(
                    "SELECT sql FROM sqlite_master WHERE type='table' AND name='users'"
                );
                
                if (tables.next()) {
                    String createTableSql = tables.getString("sql");
                    if (createTableSql != null) {
                        // Check if TEACHER is in the CHECK constraint
                        boolean hasTeacher = createTableSql.toUpperCase().contains("TEACHER");
                        boolean hasCheckConstraint = createTableSql.toUpperCase().contains("CHECK");
                        
                        // If there's a CHECK constraint but TEACHER is not in it, we need to update
                        return hasCheckConstraint && !hasTeacher;
                    }
                }
            }
        }
        return false;
    }
    
    private void updateRoleConstraint(Connection conn) throws Exception {
        // SQLite doesn't support ALTER TABLE to modify CHECK constraints directly
        // We need to recreate the table
        conn.setAutoCommit(false);
        
        try (Statement stmt = conn.createStatement()) {
            // Step 1: Create new table with updated constraint
            stmt.executeUpdate(
                "CREATE TABLE users_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100), " +
                "role VARCHAR(20) NOT NULL CHECK(role IN ('ADMIN', 'REGISTRAR', 'STAFF', 'TEACHER')), " +
                "is_active BOOLEAN NOT NULL DEFAULT 1, " +
                "created_at TIMESTAMP NOT NULL, " +
                "updated_at TIMESTAMP, " +
                "last_login TIMESTAMP" +
                ")"
            );
            
            // Step 2: Copy data from old table
            stmt.executeUpdate(
                "INSERT INTO users_new (id, username, password, full_name, email, role, is_active, created_at, updated_at, last_login) " +
                "SELECT id, username, password, full_name, email, role, is_active, created_at, updated_at, last_login FROM users"
            );
            
            // Step 3: Drop old table
            stmt.executeUpdate("DROP TABLE users");
            
            // Step 4: Rename new table
            stmt.executeUpdate("ALTER TABLE users_new RENAME TO users");
            
            // Step 5: Recreate indexes
            try {
                stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username)");
            } catch (Exception e) {
                // Index might already exist, ignore
            }
            
            conn.commit();
            System.out.println("✅ Successfully updated users table schema");
            
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}

