const { ObjectId } = require('mongodb');

class Chatroom {
    // ChatGPT Usage: Partial
    constructor(obj) {
        this._id = obj._id;
        this.messages = obj.messages ?? [];
        this.user1Id = obj.user1Id;
        this.user2Id = obj.user2Id;

    }

    toObject() {
        const transformed = {
            _id : this._id.toString(),
            messages : this.messages,
            user1Id : this.user1Id,
            user2Id : this.user2Id,
        }
        return transformed ;
    }
    
    // ChatGPT Usage: Partial
    save() {
        const index = mockedChats.findIndex(c => c._id.equals(this._id));
        if (index !== -1) {
            mockedChats[index] = {
            ...mockedChats[index],
            ...this,
            };
        } else {
            mockedChats.push(this);
        }
    }
}

// ChatGPT Usage: Partial
const mockChat0 = new Chatroom({
    _id: new ObjectId('5f9d88b9d4b4d4c6a0b0f6a8'),
    messages: [
      {
        sourceUserId: '5f9d88b9d4b4d4c6a0b0f6a0',
        content: 'User 1',
      },
      {
        sourceUserId: '5f9d88b9d4b4d4c6a0b0f6a1',
        content: 'User 2',
      },
    ],
    user1Id: '5f9d88b9d4b4d4c6a0b0f6a0', 
    user2Id: '5f9d88b9d4b4d4c6a0b0f6a1', 
  });
  
  const mockChat1 = new Chatroom({
    _id: new ObjectId('5f9d88b9d4b4d4c6a0b0f6a9'),
    messages: [
        {
          sourceUserId: '5f9d88b9d4b4d4c6a0b0f6a2',
          content: 'User 1',
        },
        {
          sourceUserId: '5f9d88b9d4b4d4c6a0b0f6a3',
          content: 'User 2',
        },
      ],
      user1Id: '5f9d88b9d4b4d4c6a0b0f6a2', 
      user2Id: '5f9d88b9d4b4d4c6a0b0f6a3', 
  });

  const mockChat2 = new Chatroom({
    _id: new ObjectId('5f9d88b9d4b4d4c6a0b0f6b1'),
    messages: [
    ],
      user1Id: '5f9d88b9d4b4d4c6a0b0f6a7', 
      user2Id: '5f9d88b9d4b4d4c6a0b0f6a6', 
  });
const mockedChats = [mockChat0, mockChat1, mockChat2];  

// ChatGPT Usage: Partial
Chatroom.findById = jest.fn().mockImplementation((id) =>{
    if(id === "errorId"){
        throw new Error('Chatroom not found');
    }
    let chatroom = mockedChats.find((chatroom) => chatroom._id == id);

    return chatroom;

})

// ChatGPT Usage: Partial
Chatroom.create = jest.fn().mockImplementation(({messages, user1Id, user2Id}) =>{
    let chatroom = new Chatroom({
        _id: new ObjectId('5f9d88b9d4b4d4c6a0b0f6b3'),
        messages,
        user1Id,
        user2Id
     })
     mockedChats.push(chatroom)

     return chatroom;
 })

 Chatroom.findOne = jest.fn().mockImplementation((query) =>{
  if (!query) return mockedChats[0];
  
  const { $or } = query;
  for(let i = 0; i < $or.length; i++){
      const { user1Id, user2Id } = $or[i];
      let chatroom = mockedChats.find((chatroom) => chatroom.user1Id === user1Id && chatroom.user2Id === user2Id);
      if(chatroom){
          return chatroom;
      }
  }
  return null;
 });

module.exports = { Chatroom, mockedChats };
  