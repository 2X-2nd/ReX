import db from '../db';

// Start a new chat
export function startChat(buyerId: string, sellerId: string) {
    const result = db.query("INSERT INTO chats (buyer_id, seller_id) VALUES (?, ?)", [buyerId, sellerId]);
    return (result as any).insertId; // Return new chat ID
}

export async function findChat(buyerId: string, sellerId: string) {
    const chat = await db.query("SELECT chat_id FROM chats WHERE seller_id = ? AND buyer_id = ?", [sellerId, buyerId]);
    return chat;
}

// Get chat history with chatId
export function getChatHistory(chatId: number) {
    const messages = db.query("SELECT * FROM messages WHERE chat_id = ? ORDER BY timestamp ASC", [chatId]);
    return messages;
}

// Get chat history with userId
export function getUserChats(userId: string) {
    const chats = db.query(`
        SELECT c.id
        FROM chats c
        WHERE c.buyer_id = ? OR c.seller_id = ?
    `, [userId, userId]);
    return chats;
}

// Send a message
export function sendMessage(chatId: number, senderId: string, message: string) {
    db.query("INSERT INTO messages (chat_id, sender_id, message) VALUES (?, ?, ?)", [chatId, senderId, message]);
}
