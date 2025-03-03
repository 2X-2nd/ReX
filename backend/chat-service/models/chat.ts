import mongoose, { Schema, Document } from 'mongoose';

export interface IChat extends Document {
    participants: {
        buyer: string;
        seller: string;
    };
    messages: Array<{
        senderId: string;
        content: string;
        timestamp: Date;
    }>;
    created: Date;
}

const ChatSchema: Schema = new Schema({
    participants: {
        type: new Schema({
            buyer: { type: String, required: [true, 'Buyer ID is required'] },
            seller: { type: String, required: [true, 'Seller ID is required'] }
        }),
        required: [true, 'Participants are required']
    },
    messages: [{
        senderId: { type: String, required: [true, 'Sender ID is required'] },
        content: { type: String, required: [true, 'Message content is required'] },
        timestamp: { type: Date, default: Date.now }
    }],
    created: { type: Date, default: Date.now }
});

export default mongoose.model<IChat>('Chat', ChatSchema);
