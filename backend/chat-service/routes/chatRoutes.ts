import express from 'express';
import { startChat, findChat, getChatHistory, getUserChats, sendMessage } from '../models/chat';
import wsServer from '../websocket';
import { addUsersToChat } from '../websocket';

const router = express.Router();

// Start a new chat
router.post('/chat/start', async (req, res) => {
    try {
        const { sellerId, buyerId } = req.body;

        if (!sellerId || !buyerId) {
            return res.status(400).json({ error: "Missing sellerId or buyerId" });
        }

        // Check if chat already exists
        let chats = await findChat(sellerId, buyerId);

        let chatId;
        if (chats.length > 0) {
            chatId = chats[0];
        } else {
            // Create new chat if it doesn't exist
            const result = startChat(sellerId, buyerId);
            chatId = result;
        }

        console.log(`Chat ${chatId} started between ${sellerId} and ${buyerId}`);

        // Add users to WebSocket room
        addUsersToChat(chatId.toString(), []);  // Clients will be added when they connect

        res.status(200).json({ chatId });

    } catch (error) {
        console.error("Error starting chat:", error);
        res.status(500).json({ error: "Internal server error" });
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

        // Notify WebSocket clients
        if (wsServer.clients) {
            wsServer.clients.forEach((client: { readyState: number; send: (arg0: string) => void; }) => {
                if (client.readyState === 1) { // WebSocket.OPEN
                    client.send(JSON.stringify({ chatId, senderId, message }));
                }
            });
        }

        res.status(201).json({ success: "Message sent" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Database error" });
    }
});

export default router;
