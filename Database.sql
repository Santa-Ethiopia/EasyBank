-- Banking App Database Setup
CREATE DATABASE IF NOT EXISTS banking_app;
USE banking_app;

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS agents;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- Create Roles table
CREATE TABLE roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_role VARCHAR(50) UNIQUE NOT NULL
);

-- Create Users table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- Create Accounts table
CREATE TABLE accounts (
    account_number VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    birth_date DATE,
    national_id VARCHAR(50) UNIQUE,
    city VARCHAR(100),
    sub_city VARCHAR(100),
    woreda VARCHAR(100),
    mothers_name VARCHAR(100),
    mobile_number VARCHAR(20),
    email VARCHAR(100),
    registration_date DATETIME,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    account_balance DECIMAL(15,2) DEFAULT 0.00,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Create Transactions table
CREATE TABLE transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    account_balance DECIMAL(15,2),
    transaction_date DATETIME,
    performed_by VARCHAR(100),
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);

-- Create Agents table
CREATE TABLE agents (
    agent_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_name VARCHAR(100),
    business_name VARCHAR(100),
    tin VARCHAR(50) UNIQUE,
    agent_balance DECIMAL(15,2) DEFAULT 0.00,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Insert initial data
INSERT INTO roles (user_role) VALUES 
('ROLE_ADMIN'),
('ROLE_CUSTOMER'),
('ROLE_AGENT');

-- Insert admin user (password: admin123)
INSERT INTO users (username, password, role_id, status) 
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 1, 'ACTIVE');

-- Display created tables
SHOW TABLES;

-- Verify data
SELECT * FROM roles;
SELECT * FROM users;




What We'll Continue Tomorrow:
Agent functionality and features

Transaction history improvements

Any additional features you'd like to add

Testing and refinements