-- Tạo bảng Users
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('family', 'nurse', 'admin') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15),
    address TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE,
    INDEX idx_role (role)
);

-- Tạo bảng FamilyProfiles
CREATE TABLE FamilyProfiles (
    family_profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    family_size INT,
    specific_needs TEXT,
    preferred_location VARCHAR(100),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Tạo bảng NurseProfiles
CREATE TABLE NurseProfiles (
    nurse_profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    location VARCHAR(100) NOT NULL,
    skills TEXT NOT NULL,
    experience_years INT NOT NULL,
    certifications TEXT,
    availability TEXT NOT NULL,
    hourly_rate DECIMAL(10, 2) NOT NULL,
    bio TEXT,
    profile_image VARCHAR(255) DEFAULT 'default_profile.jpg',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Tạo bảng Services (Dịch vụ)
CREATE TABLE Services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    nurse_user_id INT NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    availability_schedule TEXT NOT NULL,
    status ENUM('active', 'inactive') DEFAULT 'active',
    FOREIGN KEY (nurse_user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Tạo bảng Bookings
CREATE TABLE Bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    family_user_id INT NOT NULL,
    nurse_user_id INT NOT NULL,
    service_id INT NOT NULL,
    service_type ENUM('hourly', 'daily', 'weekly') NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('pending', 'accepted', 'completed', 'cancelled') DEFAULT 'pending',
    total_cost DECIMAL(10, 2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (family_user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (nurse_user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES Services(service_id) ON DELETE CASCADE,
    INDEX idx_booking_family (family_user_id),
    INDEX idx_booking_nurse (nurse_user_id)
);

-- Tạo bảng Feedbacks
CREATE TABLE Feedbacks (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    family_user_id INT NOT NULL,
    nurse_user_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    response TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (family_user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (nurse_user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    INDEX idx_feedback_nurse (nurse_user_id),
    INDEX idx_feedback_family (family_user_id)
);

-- Tạo bảng Messages
CREATE TABLE Messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    attachment VARCHAR(255),
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    INDEX idx_message_sender (sender_id),
    INDEX idx_message_receiver (receiver_id)
);

-- Tạo bảng Earnings (Thu nhập)
CREATE TABLE Earnings (
    earning_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    nurse_user_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    platform_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    net_income DECIMAL(10, 2) GENERATED ALWAYS AS (amount - platform_fee) STORED,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (nurse_user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Tạo bảng AdminActions
CREATE TABLE AdminActions (
    action_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id INT NOT NULL,
    action_type ENUM('approve_user', 'resolve_dispute', 'other') NOT NULL,
    target_user_id INT NOT NULL,
    description TEXT,
    action_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Tạo bảng Reports
CREATE TABLE Reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id INT NOT NULL,
    report_type ENUM('service_demand', 'user_activity', 'platform_performance') NOT NULL,
    data TEXT,
    generated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Tạo bảng Disputes (Giải quyết tranh chấpchấp)
CREATE TABLE Disputes (
    dispute_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    raised_by_user_id INT NOT NULL,
    description TEXT NOT NULL,
    status ENUM('open', 'resolved', 'closed') DEFAULT 'open',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (raised_by_user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Trigger
DELIMITER $$

-- Trigger cho FamilyProfiles
CREATE TRIGGER before_family_insert
BEFORE INSERT ON FamilyProfiles
FOR EACH ROW
BEGIN
    DECLARE user_role ENUM('family', 'nurse', 'admin');
    SELECT role INTO user_role FROM Users WHERE user_id = NEW.user_id;
    IF user_role IS NULL OR user_role <> 'family' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Chỉ người dùng có role family mới có thể thêm vào FamilyProfiles';
    END IF;
END $$

-- Trigger cho NurseProfiles
CREATE TRIGGER before_nurse_insert
BEFORE INSERT ON NurseProfiles
FOR EACH ROW
BEGIN
    DECLARE user_role ENUM('family', 'nurse', 'admin');
    SELECT role INTO user_role FROM Users WHERE user_id = NEW.user_id;
    IF user_role IS NULL OR user_role <> 'nurse' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Chỉ người dùng có role nurse mới có thể thêm vào NurseProfiles';
    END IF;
END $$

-- Trigger cho Services
CREATE TRIGGER before_service_insert
BEFORE INSERT ON Services
FOR EACH ROW
BEGIN
    DECLARE nurse_role ENUM('family', 'nurse', 'admin');
    SELECT role INTO nurse_role FROM Users WHERE user_id = NEW.nurse_user_id;
    IF nurse_role <> 'nurse' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'nurse_user_id phải có role nurse';
    END IF;
END $$

-- Trigger cho Bookings
CREATE TRIGGER before_booking_insert
BEFORE INSERT ON Bookings
FOR EACH ROW
BEGIN
    DECLARE family_role ENUM('family', 'nurse', 'admin');
    DECLARE nurse_role ENUM('family', 'nurse', 'admin');
    SELECT role INTO family_role FROM Users WHERE user_id = NEW.family_user_id;
    SELECT role INTO nurse_role FROM Users WHERE user_id = NEW.nurse_user_id;
    IF family_role <> 'family' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'family_user_id phải có role family';
    END IF;
    IF nurse_role <> 'nurse' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'nurse_user_id phải có role nurse';
    END IF;
    IF NEW.start_time >= NEW.end_time THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'start_time phải nhỏ hơn end_time';
    END IF;
END $$

-- Trigger cho Feedbacks
CREATE TRIGGER before_feedback_insert
BEFORE INSERT ON Feedbacks
FOR EACH ROW
BEGIN
    DECLARE family_role ENUM('family', 'nurse', 'admin');
    DECLARE nurse_role ENUM('family', 'nurse', 'admin');
    SELECT role INTO family_role FROM Users WHERE user_id = NEW.family_user_id;
    SELECT role INTO nurse_role FROM Users WHERE user_id = NEW.nurse_user_id;
    IF family_role <> 'family' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'family_user_id phải có role family';
    END IF;
    IF nurse_role <> 'nurse' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'nurse_user_id phải có role nurse';
    END IF;
END $$

-- Trigger cho Earnings
CREATE TRIGGER before_earning_insert
BEFORE INSERT ON Earnings
FOR EACH ROW
BEGIN
    DECLARE nurse_role ENUM('family', 'nurse', 'admin');
    SELECT role INTO nurse_role FROM Users WHERE user_id = NEW.nurse_user_id;
    IF nurse_role <> 'nurse' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'nurse_user_id phải có role nurse';
    END IF;
END $$

-- Trigger cho AdminActions
CREATE TRIGGER before_admin_action_insert
BEFORE INSERT ON AdminActions
FOR EACH ROW
BEGIN
    DECLARE admin_role ENUM('family', 'nurse', 'admin');
    SELECT role INTO admin_role FROM Users WHERE user_id = NEW.admin_user_id;
    IF admin_role <> 'admin' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'admin_user_id phải có role admin';
    END IF;
END $$

-- Trigger cho Reports
CREATE TRIGGER before_report_insert
BEFORE INSERT ON Reports
FOR EACH ROW
BEGIN
    DECLARE admin_role ENUM('family', 'nurse', 'admin');
    SELECT role INTO admin_role FROM Users WHERE user_id = NEW.admin_user_id;
    IF admin_role <> 'admin' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'admin_user_id phải có role admin';
    END IF;
END $$

-- Trigger cho Disputes
CREATE TRIGGER before_dispute_insert
BEFORE INSERT ON Disputes
FOR EACH ROW
BEGIN
    DECLARE raised_by_role ENUM('family', 'nurse', 'admin');
    SELECT role INTO raised_by_role FROM Users WHERE user_id = NEW.raised_by_user_id;
    IF raised_by_role NOT IN ('family', 'nurse') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'raised_by_user_id phải có role family hoặc nurse';
    END IF;
END $$

DELIMITER ;
