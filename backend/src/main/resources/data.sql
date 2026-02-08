-- Money Transfer System Seed Data

-- Insert sample accounts with usernames matching Spring Security users
INSERT IGNORE INTO accounts (id, username, holder_name, balance, status, version) VALUES 
    (1, 'admin', 'Admin', 0.00, 'ACTIVE', 0),
    (2, 'Partha', 'Partha', 300000.00, 'ACTIVE', 0),
    (3, 'Nidhi', 'Nidhi', 1000000.00, 'ACTIVE', 0),
    (4, 'Krishna', 'Krishna', 150000.00, 'ACTIVE', 0),
    (5, 'Prakhar', 'Prakhar', 750000.00, 'ACTIVE', 0);
