import mysql from 'mysql2';

const db = mysql.createPool({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

db.getConnection((err: unknown, connection: any) => {
    if (err) {
        console.error('❌ Database connection failed:', err)
        return
    }
    console.log('✅ Connected to MySQL')
    connection.release();
})

export default db;
