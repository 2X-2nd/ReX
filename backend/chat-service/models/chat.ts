import { RowDataPacket } from 'mysql2';
import db from '../db';

// Start a new chat
export async function startChat(buyerId: string, sellerId: string) {
    const [result] = await db.query("INSERT INTO chats (buyer_id, seller_id) VALUES (?, ?)", [buyerId, sellerId]);
    return (result as any).insertId; // Return new chat ID
}

export async function findChat(buyerId: string, sellerId: string) {
    const [chats] = await db.query<RowDataPacket[]>("SELECT id FROM chats WHERE seller_id = ? AND buyer_id = ?", [sellerId, buyerId]);
    return chats.length > 0 ? chats[0].id : null;  // Return only the ID
}

// Get chat history with chatId
export async function getChatHistory(chatId: number) {
    const [messages] = await db.query("SELECT * FROM messages WHERE chat_id = ? ORDER BY timestamp ASC", [chatId]);
    return messages;
}

// Get chat history with userId
export async function getUserChats(userId: string) {
    const [chats] = await db.query(`
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
