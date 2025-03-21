import express from 'express';
import chatRoutes from './routes/chatRoutes';
import dotenv from 'dotenv';
dotenv.config();

const app = express();
app.use(express.json()); // Middleware to parse JSON
app.use(chatRoutes); // Register chat routes

const PORT = process.env.CHAT_PORT ?? 5000;
if (process.env.NODE_ENV !== 'test') {
    app.listen(PORT, () => {
        console.log(`Chat service running on http://localhost:${PORT}`);
    });
}

export default app;