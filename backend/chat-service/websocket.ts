import WebSocket from 'ws';
import db from './db';

// Create WebSocket server on port 8001
const wsServer = new WebSocket.Server({ port: 8001 });

// Store connected clients
const clients: Record<string, WebSocket[]> = {}; // { chatId: [clients] }

wsServer.on('connection', (ws) => {
    console.log('New WebSocket connection established');

    ws.on('message', async (data) => {
        try {
            const { chatId, senderId, message } = JSON.parse(data.toString());

            if (!chatId || !senderId) {
                console.log("Invalid WebSocket message received:", data.toString());
                return;
            }

            // Ensure chatId is tracked
            if (!clients[chatId]) {
                clients[chatId] = [];
            }

            // Add WebSocket to chat room if not already added
            if (!clients[chatId].includes(ws)) {
                clients[chatId].push(ws);
            }

            console.log(`Message in chat ${chatId}: ${message} from ${senderId}`);

            // Store message in database
            if (message) {
                await db.query(
                    "INSERT INTO messages (chat_id, sender_id, message) VALUES (?, ?, ?)",
                    [chatId, senderId, message]
                );
            }

            // Broadcast message to all connected clients in the same chat
            clients[chatId].forEach(client => {
                if (client !== ws && client.readyState === WebSocket.OPEN) {
                    client.send(JSON.stringify({ chatId, senderId, message }));
                }
            });

        } catch (error) {
            console.error("Error handling WebSocket message:", error);
        }
    });

    ws.on('close', () => {
        console.log('WebSocket client disconnected');
        for (const chatId in clients) {
            clients[chatId] = clients[chatId].filter(client => client !== ws);
        }
    });
});

// Function to add users to WebSocket chat when chat starts
export const addUsersToChat = (chatId: string, userSockets: WebSocket[]) => {
    if (!clients[chatId]) {
        clients[chatId] = [];
    }

    userSockets.forEach(ws => {
        if (!clients[chatId].includes(ws)) {
            clients[chatId].push(ws);
        }
    });

    console.log(`Users added to WebSocket chat ${chatId}`);
};

export default wsServer;
