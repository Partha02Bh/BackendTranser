-- Money Transfer System Seed Data

-- Insert sample accounts with usernames matching Spring Security users
INSERT IGNORE INTO accounts (id, username, holder_name, balance, status, version) VALUES 
    (1, 'admin', 'Admin User', 5000.00, 'ACTIVE', 0),
    (2, 'user', 'Regular User', 3000.00, 'ACTIVE', 0),
    (3, 'alice', 'Alice Smith', 10000.00, 'ACTIVE', 0),
    (4, 'bob', 'Bob Johnson', 1500.00, 'ACTIVE', 0),
    (5, 'charlie', 'Charlie Brown', 7500.00, 'ACTIVE', 0),
    (6, 'diana', 'Diana Prince', 12000.00, 'ACTIVE', 0),
    (7, 'emma', 'Emma Watson', 8500.00, 'ACTIVE', 0);
