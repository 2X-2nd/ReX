jest.mock('../models/chat');
import request from 'supertest';
import express from 'express';
import chatRoutes from '../routes/chatRoutes';
import { startChat, findChat, getChatHistory, getUserChats, sendMessage } from '../models/chat';

jest.mock('ws', () => {
    return {
        Server: jest.fn().mockImplementation(() => ({
            on: jest.fn(),
            emit: jest.fn(),
            to: jest.fn().mockReturnThis(),
        })),
    };
});

jest.mock('../db', () => ({
    query: jest.fn().mockResolvedValue([{}]),
    end: jest.fn().mockResolvedValue(null),
}));

const app = express();
app.use(express.json());
app.use('/api', chatRoutes);

afterEach(() => {
    jest.clearAllMocks();
});

// Tests with mocking
describe('POST /api/chat/start (With Mock)', () => {
    // Mocked behavior: Successfully start a chat
    // Input: { buyerId: 'buyer123', sellerId: 'seller456' }
    // Expected status code: 200
    // Expected output: { chatId: 'mockChatId' }
    test('should return 200 and mock chatId', async () => {
        (startChat as jest.Mock).mockResolvedValue('mockChatId');
        (findChat as jest.Mock).mockResolvedValue(null);

        const response = await request(app).post('/api/chat/start').send({
            buyerId: 'buyer123',
            sellerId: 'seller456'
        });

        expect(response.status).toBe(200);
        expect(response.body).toEqual({ chatId: 'mockChatId' });
    });

    // Mocked behavior: Start a chat with database failure
    // Input: { buyerId: '123', sellerId: '456' }
    // Expected status code: 500
    // Expected output: { error: 'Internal server error' }
    test('should return 500 if database error occurs', async () => {
        (startChat as jest.Mock).mockRejectedValue(new Error('Database error'));

        const response = await request(app).post('/api/chat/start').send({
            buyerId: "123",
            sellerId: "456"
        });

        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: 'Internal server error' });
    });
});

describe('GET /api/chat/:chatId (With Mock)', () => {
    // Mocked behavior: Successfully retrieve messages from a chat
    // Input: GET /api/chat/123
    // Expected status code: 200
    // Expected output: { messages: [{ senderId: 'user1', message: 'Hello' }] }
    test('should return 200 and chat messages', async () => {
        (getChatHistory as jest.Mock).mockResolvedValue([{ senderId: 'user1', message: 'Hello' }]);

        const response = await request(app).get('/api/chat/123');

        expect(response.status).toBe(200);
        expect(response.body).toEqual({ messages: [{ senderId: 'user1', message: 'Hello' }] });
    });

    // Mocked behavior: Database failure when retrieving messages
    // Input: GET /api/chat/123
    // Expected status code: 500
    // Expected output: { error: 'Database error' }
    test('should return 500 if database error occurs', async () => {
        (getChatHistory as jest.Mock).mockRejectedValue(new Error('Database error'));

        const response = await request(app).get('/api/chat/123');

        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: 'Database error' });
    });

    // Mocked behavior: Retrieving messages with empty chat id
    // Input: GET /api/chat
    // Expected status code: 400
    // Expected output: { error: 'Invalid Chat ID' }
    test('should return 400 if missing chat id', async () => {
        const response = await request(app).get('/api/chat/NaN');

        expect(response.status).toBe(400);
        expect(response.body).toEqual({ error: 'Invalid Chat ID' });
    });
});

describe('GET /api/chat/user/:userId (With Mock)', () => {
    // Mocked behavior: Get chat id with user id
    // Input: GET /api/chat/user/user123
    // Expected status code: 200
    // Expected output: { chatIds: ['chat1', 'chat2'] }
    test('should return 200 and user chat IDs', async () => {
        (getUserChats as jest.Mock).mockResolvedValue(['chat1', 'chat2']);

        const response = await request(app).get('/api/chat/user/user123');

        expect(response.status).toBe(200);
        expect(response.body).toEqual({ chatIds: ['chat1', 'chat2'] });
    });

    // Mocked behavior: Database failure when retrieving chat id
    // Input: GET /api/chat/user/user123
    // Expected status code: 500
    // Expected output: { error: 'Database error' }
    test('should return 500 if database error occurs', async () => {
        (getUserChats as jest.Mock).mockRejectedValue(new Error('Database error'));

        const response = await request(app).get('/api/chat/user/user123');

        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: 'Database error' });
    });
});

describe('POST /api/chat/:chatId/message (With Mock)', () => {
    // Mocked behavior: Send message in a chat
    // Input: { senderId: '123', message: 'Hello' } to POST /api/chat/123/message
    // Expected status code: 201
    // Expected output: { success: 'Message sent' }
    test('should return 201 on successful message send', async () => {
        (sendMessage as jest.Mock).mockResolvedValue('mockChatId');

        const response = await request(app).post('/api/chat/123/message').send({
            senderId: '123',
            message: 'Hello'
        });

        expect(response.status).toBe(201);
        expect(response.body).toEqual({ success: 'Message sent' });
    });

    // Mocked behavior: Database failure when sending a message
    // Input: { senderId: '123', message: 'Hello' } to POST /api/chat/123/message
    // Expected status code: 500
    // Expected output: { error: 'Database error' }
    test('should return 500 if database error occurs', async () => {
        (sendMessage as jest.Mock).mockRejectedValue(new Error('Database error'));

        const response = await request(app).post('/api/chat/123/message').send({
            senderId: '123',
            message: 'Hello'
        });

        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: 'Database error' });
    });

    // Mocked behavior: Send message with no input
    // Input: no input
    // Expected status code: 400
    // Expected output: { error: "Chat ID, sender ID, and message are required" }
    test('should return 201 on successful message send', async () => {
        (sendMessage as jest.Mock).mockResolvedValue('mockChatId');

        const response = await request(app).post('/api/chat/123/message').send({});

        expect(response.status).toBe(400);
        expect(response.body).toEqual({ error: "Chat ID, sender ID, and message are required" });
    });
});
