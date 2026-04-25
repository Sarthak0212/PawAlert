-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    contact VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Animal Reports Table
CREATE TABLE IF NOT EXISTS animal_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    animal_name VARCHAR(100),
    location VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    report_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Rescue Details Table
CREATE TABLE IF NOT EXISTS rescue_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    rescue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status_updates TEXT,
    outcome VARCHAR(255),
    notes TEXT,
    FOREIGN KEY (report_id) REFERENCES animal_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (volunteer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Sample Data
INSERT INTO users (name, email, password, role, contact) VALUES 
('John Doe', 'john@example.com', 'password123', 'REPORTER', '1234567890'),
('Alice Smith', 'alice@example.com', 'password123', 'VOLUNTEER', '0987654321');

INSERT INTO animal_reports (reporter_id, animal_name, location, description, status) VALUES 
(1, 'Stray Dog', 'Main Street, Central Park', 'Injured leg, needs immediate help.', 'IN_PROGRESS'),
(1, 'Kitten', 'Old Warehouse, Dockyard', 'Stuck on a high shelf.', 'PENDING');

INSERT INTO rescue_details (report_id, volunteer_id, status_updates, outcome, notes) VALUES 
(1, 2, 'Reached location, dog is friendly but scared.', 'UNDER_TREATMENT', 'Dog moved to Vet Clinic.');
