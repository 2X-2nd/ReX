import express from 'express';
import { startChat, getChatHistory, getUserChats, sendMessage } from '../models/chat';

const router = express.Router();

// Start a new chat
router.post('/chat/start', async (req, res) => {
    const { buyerId, sellerId } = req.body;
    if (!buyerId || !sellerId) {
        return res.status(400).json({ error: "Buyer ID and Seller ID are required" });
    }

    try {
        const chatId = await startChat(buyerId, sellerId);
        res.status(201).json({ chatId });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Database error" });
    }
});

// Get chat history with chatId
router.get('/chat/:chatId', async (req, res) => {
    const chatId = parseInt(req.params.chatId);
    if (!chatId) {
        return res.status(400).json({ error: "Invalid Chat ID" });
    }

    try {
        const messages = await getChatHistory(chatId);
        res.status(200).json({ messages });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Database error" });
    }
});

// Get chat history with userId
router.get('/chat/user/:userId', async (req, res) => {
    const userId = req.params.userId;
    if (!userId) { return res.status(400).json({ error: "Invalid User ID" }); }
    try {
        const chatIds = await getUserChats(userId);
        res.status(200).json({ chatIds });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Database error" });
    }
});

// Send a message
router.post('/chat/:chatId/message', async (req, res) => {
    const chatId = parseInt(req.params.chatId);
    const { senderId, message } = req.body;

    if (!chatId || !senderId || !message) {
        return res.status(400).json({ error: "Chat ID, sender ID, and message are required" });
    }

    try {
        await sendMessage(chatId, senderId, message);
        res.status(201).json({ success: "Message sent" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Database error" });
    }
});

export default router;
