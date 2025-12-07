package com.enrollment.system.config;

import com.enrollment.system.util.DatabaseSchemaUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener to fix database schema after application is fully ready.
 * This ensures the fix runs after Hibernate has initialized.
 */
@Component
public class SchemaFixListener {
    
    @Autowired(required = false)
    private DatabaseSchemaUpdater databaseSchemaUpdater;
    
    @Autowired(required = false)
    private ApplicationContext applicationContext;
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (databaseSchemaUpdater != null) {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("POST-INITIALIZATION SCHEMA FIX");
            System.out.println("=".repeat(80));
            System.out.println("Running schema fix after Hibernate initialization...");
            
            try {
                // Check and fix the table schema
                databaseSchemaUpdater.ensureTeacherAssignmentsTableExists();
                System.out.println("✅ Post-initialization schema check completed");
            } catch (Exception e) {
                System.err.println("❌ Post-initialization schema fix failed: " + e.getMessage());
                // Try force fix as last resort
                try {
                    System.out.println("🔄 Attempting force fix as last resort...");
                    databaseSchemaUpdater.forceFixTeacherAssignmentsTable();
                } catch (Exception e2) {
                    System.err.println("❌ Force fix also failed: " + e2.getMessage());
                }
            }
            
            // Also run comprehensive schema scanner
            if (applicationContext != null) {
                try {
                    com.enrollment.system.util.DatabaseSchemaFixer fixer = 
                        applicationContext.getBean(com.enrollment.system.util.DatabaseSchemaFixer.class);
                    if (fixer != null) {
                        fixer.scanAndFixTeacherAssignmentsTable();
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Schema scanner not available or failed: " + e.getMessage());
                }
            }
            
            System.out.println("=".repeat(80) + "\n");
        }
    }
}

