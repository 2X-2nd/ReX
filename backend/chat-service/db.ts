import mysql from 'mysql2';

const db = mysql.createPool({
    host: process.env.DB_NAME,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_HOST,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
}).promise(); // Enables async/await support

export default db;
