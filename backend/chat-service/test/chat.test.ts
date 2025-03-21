import request from 'supertest';
import mysql from 'mysql2'
import app from '../server'
require("dotenv").config();


describe('Chat Routes (Non-Mocking)', () => {
    beforeAll(async () => {
        const db = mysql.createPool({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME
        });
    });

    describe('POST /chat/start', () => {
        // Input: buyerId = 'buyer123', sellerId = 'seller456'
        // Expected status code: 201
        // Expected behavior: A new chat is created and stored in the database
        // Expected output: { chatId: '<some_chat_id>' }
        test('should return 201 and create a new chat', async () => {
            const response = await request(app).post("/chat/start").send({
                buyerId: "buyer123",
                sellerId: "seller456"
            });

            expect(response.status).toBe(201);
            expect(response.body).toHaveProperty('chatId');
        });

        // Input: buyerId is provided but sellerId is missing
        // Expected status code: 400
        // Expected behavior: Request is rejected due to missing required fields
        // Expected output: { error: "Buyer ID and Seller ID are required" }
        test('should return 400 if buyerId or sellerId is missing', async () => {
            const response = await request(app).post('/chat/start').send({
                buyerId: 'buyer123'
            });

            expect(response.status).toBe(400);
            expect(response.body).toEqual({ error: "Buyer ID and Seller ID are required" });
        });
    });

    describe('GET /chat/:chatId', () => {
        // Input: chatId = 1 (valid chat ID)
        // Expected status code: 200
        // Expected behavior: Chat history is retrieved from the database
        // Expected output: { messages: [{ senderId: 'user1', message: 'Hello' }, ...] }
        test('should return 200 and chat messages', async () => {
            // Ensure there's an existing chat with messages in the database
            const chatId = 1;
            const response = await request(app).get(`/chat/${chatId}`);

            expect(response.status).toBe(200);
            expect(response.body).toHaveProperty('messages');
            expect(Array.isArray(response.body.messages)).toBe(true);
        });

        // Input: chatId is an invalid string (e.g., 'invalidId')
        // Expected status code: 400
        // Expected behavior: Request is rejected due to invalid chat ID format
        // Expected output: { error: "Invalid Chat ID" }
        test('should return 400 if chatId is invalid', async () => {
            const response = await request(app).get('/chat/invalidId');

            expect(response.status).toBe(400);
            expect(response.body).toEqual({ error: "Invalid Chat ID" });
        });
    });

    describe('GET /chat/user/:userId', () => {
        // Input: userId = 'user123' (valid user ID)
        // Expected status code: 200
        // Expected behavior: Returns all chat IDs associated with the user
        // Expected output: { chatIds: ['chat1', 'chat2', ...] }
        test('should return 200 and chat IDs for a user', async () => {
            const userId = 'buyer123';
            const response = await request(app).get(`/chat/user/${userId}`);

            expect(response.status).toBe(200);
            expect(response.body).toHaveProperty('chatIds');
            expect(Array.isArray(response.body.chatIds)).toBe(true);
        });
    });

    describe('POST /chat/:chatId/message', () => {
        // Input: chatId = 1, senderId = 'user123', message = 'Hello'
        // Expected status code: 201
        // Expected behavior: Message is stored in the chat conversation
        // Expected output: { success: "Message sent" }
        test('should return 201 when a message is sent', async () => {
            const chatId = 1;
            const response = await request(app).post(`/chat/${chatId}/message`).send({
                senderId: 'user123',
                message: 'Hello'
            });

            expect(response.status).toBe(201);
            expect(response.body).toEqual({ success: "Message sent" });
        });

        // Input: chatId = 1, senderId is missing, message = 'Hello'
        // Expected status code: 400
        // Expected behavior: Request is rejected due to missing senderId
        // Expected output: { error: "Chat ID, sender ID, and message are required" }
        test('should return 400 if chatId, senderId, or message is missing', async () => {
            const chatId = 1;
            const response = await request(app).post(`/chat/${chatId}/message`).send({
                senderId: 'user123'
            });

            expect(response.status).toBe(400);
            expect(response.body).toEqual({ error: "Chat ID, sender ID, and message are required" });
        });
    });

});
