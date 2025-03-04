import express from 'express';
import chatRoutes from './routes/chatRoutes';

const app = express();
app.use(express.json()); // Middleware to parse JSON
app.use(chatRoutes); // Register chat routes

const PORT = process.env.CHAT_PORT || 3000;
app.listen(PORT, () => {
    console.log(`Chat service running on http://localhost:${PORT}`);
});
