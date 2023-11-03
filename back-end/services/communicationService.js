const { Chatroom } = require("../models/chatroom");
const { User } = require("../models/user");
const OpenAI = require('openai');

// ChatGPT Usage: No
// Add message to database
async function sendMessage(chatroomId, content, sourceUserId, learningSession){
    const chatroom = await Chatroom.findById(chatroomId);
    chatroom.messages.push({sourceUserId, content})
     
    if(learningSession){
        let openAIResponse = await openAIMessage(content)
        openAIResponse = "AI Assistant: " + openAIResponse;
	    let id = "6541a9947cce981c74b03ecb";
        chatroom.messages.push({sourceUserId: id, content: openAIResponse})
        await chatroom.save();
	    return { sourceUserId: id, content: openAIResponse };
    }

    await chatroom.save();

    return { sourceUserId: sourceUserId, content: content};;
}

// ChatGPT Usage: No
// Get all chatrooms associated with a user
async function getChatrooms(userId){
    const user = await User.findById(userId)
    const chatroomIds = user.chatroomIDs

    let chatrooms = []

    for(let chatroomId of chatroomIds){
        const chatroom = await Chatroom.findById(chatroomId)
        const chatroomObj = chatroom.toObject()

        const otherUser = (chatroom.user1Id == userId) ? await User.findById(chatroom.user2Id) : await User.findById(chatroom.user1Id)
        
        chatroomObj.displayName = user.displayName
        chatroomObj.otherUserName = otherUser.displayName
        console.log(chatroomObj)
        chatrooms.push(chatroomObj)
    }

    return chatrooms
}

// Get all chatrooms associated with a user
async function getMessages(chatroomId){
    const chatroom = await Chatroom.findById(chatroomId)

    return chatroom.messages;
}

// Create a new chatroom
async function createChatroom(user1Id, user2Id){
    const user1 = await User.findById(user1Id)
    const user2 = await User.findById(user2Id)

    const chatroom = await Chatroom.create({
        messages: [],
        user1Id: user1Id,
        user2Id: user2Id
    })

    user1.chatroomIDs.push(chatroom._id)
    user2.chatroomIDs.push(chatroom._id)

    await user1.save()
    await user2.save()

    return chatroom._id
}

// ChatGPT: Partial
async function startLearningSession(){
    const openai = new OpenAI({
        apiKey: process.env.OPENAIKEY
    })
    const completion = await openai.chat.completions.create({
        messages: [
            { role: "system", content: `You are a helpful assistant that helps people learn languages. Your goal is to provide speaking prompts for a given proficiency level in a given language. The only text in your output should be in this format ["" , "" ...] The list should be of length 5.`},
            { role: "user", content: "Provide me with a list of talking points" },
        ],
        model: "gpt-3.5-turbo",
    });

    try {
        const jsonArray = JSON.parse(completion.choices[0]);
        return jsonArray;
      } catch (error) {
        console.error('Error parsing JSON:', error);
      }
    return [];
}

// ChatGPT: Partial
async function openAIMessage(message) {
	console.log("Open ai message")
    const openai = new OpenAI({
        apiKey: process.env.OPENAIKEY
    })
    const completion = await openai.chat.completions.create({
        messages: [
            { role: "system", content: "You are a helpful assistant that helps people learn languages. Your goal is to correct grammar mistakes and provide helpful tips. If the structure is correct, you should say always say 'No comment'. Whenever I send a message, you should only correct it and give tips OR say no comment." },
            { role: "user", content: "I like you to." },
            { role: "assistant", content: "The correct sentence structure in this sentence is 'I like you too'." },
            { role: "user", content: "There are a lot of people here." },
            { role: "assistant", content: "No comment." },
            { role: "user", content: "If I was there, I would have helped." },
            { role: "assistant", content: "The correct sentence structure is 'If I were there, I would have helped'." },
            { role: "user", content: message },
    ],
        model: "gpt-3.5-turbo",
    });

    return completion.choices[0].message.content;
}

module.exports = {sendMessage, getChatrooms, createChatroom, getMessages, openAIMessage, startLearningSession};
