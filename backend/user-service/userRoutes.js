import express from 'express';
import db from '../db.js';

const router = express.Router();

// POST /users/register - Registers a new user
router.post('/register', async (req, res) => {
    const { google_id, email, username, preferences, latitude, longitude } = req.body;

    try {
        const preferencesJson = JSON.stringify(preferences);

        await db.execute(
            `INSERT INTO users (google_id, email, username, preferences, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)`,
            [google_id, email, username, preferencesJson, latitude, longitude]
        );

        res.status(201).json({ message: "User registered successfully", user_id: google_id });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// GET /users/:id - Retrieves user profile
router.get('/:id', async (req, res) => {
    const { id } = req.params;

    try {
        const [rows] = await db.execute(`SELECT * FROM users WHERE google_id = ?`, [id]);

        if (rows.length === 0) {
            return res.status(404).json({ error: "User not found" });
        }

        const user = rows[0];
        console.log("Raw database response:", user);

        if (typeof user.preferences === 'string') {
            try {
                user.preferences = JSON.parse(user.preferences);
            } catch (error) {
                console.error("JSON parsing error:", error);
                user.preferences = [];
            }
        } else if (!Array.isArray(user.preferences)) {
            user.preferences = [];
        }

        console.log("Parsed preferences:", user.preferences);

        res.status(200).json(user);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// DELETE /users/:id - Deletes a user account
router.delete('/:id', async (req, res) => {
    const { id } = req.params;

    try {
        const [result] = await db.execute(`DELETE FROM users WHERE google_id = ?`, [id]);

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: "User not found" });
        }

        res.status(200).json({ message: "User deleted successfully" });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// PUT /users/:id - Updates a user's preferences
router.put('/:id', async (req, res) => {
    const { id } = req.params;
    const { email, username, preferences, latitude, longitude } = req.body;

    try {
        const preferencesJson = JSON.stringify(preferences);

        const [result] = await db.execute(
            `UPDATE users SET email = ?, username = ?, preferences = ?, latitude = ?, longitude = ? WHERE google_id = ?`,
            [email, username, preferencesJson, latitude, longitude, id]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: "User not found" });
        }

        res.status(200).json({ message: "User updated successfully" });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

export default router;
