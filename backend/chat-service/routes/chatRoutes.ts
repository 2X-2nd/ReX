import { Router, Request, Response } from 'express';
import Chat from '../models/chat';

const router = Router();

interface StartChatRequest {
    buyerId: string;
    sellerId: string;
}

router.post('/chat/start', async (req: Request, res: Response) => {
    try {
        const { buyerId, sellerId }: StartChatRequest = req.body;
        
        // Check if chat already exists
        const existingChat = await Chat.findOne({
            'participants.buyer': buyerId,
            'participants.seller': sellerId
        });

        if (existingChat) {
            return res.status(200).json(existingChat);
        }

        const newChat = new Chat({
            participants: {
                buyer: buyerId,
                seller: sellerId
            },
            messages: []
        });

        const savedChat = await newChat.save();
        res.status(201).json(savedChat);
    } catch (error) {
        res.status(500).json({ message: 'Error starting chat', error });
    }
});

router.get('/chat/:chatId', async (req: Request, res: Response) => {
    try {
        const chat = await Chat.findById(req.params.chatId);
        if (!chat) {
            return res.status(404).json({ message: 'Chat not found' });
        }
        res.json(chat);
    } catch (error) {
        res.status(500).json({ message: 'Error retrieving chat', error });
    }
});

interface SendMessageRequest {
    senderId: string;
    content: string;
}

router.post('/chat/:chatId/message', async (req: Request, res: Response) => {
    try {
        const { senderId, content }: SendMessageRequest = req.body;
        const chat = await Chat.findById(req.params.chatId);

        if (!chat) {
            return res.status(404).json({ message: 'Chat not found' });
        }

        chat.messages.push({
            senderId,
            content,
            timestamp: new Date()
        });

        const updatedChat = await chat.save();
        res.status(201).json(updatedChat);
    } catch (error) {
        res.status(500).json({ message: 'Error sending message', error });
    }
});

export default router;
