import db from '../db';

// Start a new chat
export async function startChat(buyerId: number, sellerId: number) {
    const [result] = await db.query("INSERT INTO chats (buyer_id, seller_id) VALUES (?, ?)", [buyerId, sellerId]);
    return (result as any).insertId; // Return new chat ID
}

// Get chat history
export async function getChatHistory(chatId: number) {
    const [messages] = await db.query("SELECT * FROM messages WHERE chat_id = ? ORDER BY timestamp ASC", [chatId]);
    return messages;
}

// Send a message
export async function sendMessage(chatId: number, senderId: number, message: string) {
    await db.query("INSERT INTO messages (chat_id, sender_id, message) VALUES (?, ?, ?)", [chatId, senderId, message]);
}
