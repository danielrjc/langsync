const  {mockedUsers}  = require("./mockedUsers");
class User {
    constructor(obj) {
        this.email = obj.email;
        this.displayName = obj.displayName;
        this.admin = obj.admin ?? false;
        this.registered = obj.registered;
        this.picture = obj.picture;
        this.learningPreference = obj.learningPreference ?? [];
        this.interestedLanguages = obj.interestedLanguages ?? [];
        this.proficientLanguages = obj.proficientLanguages ?? [];
        this.banned = obj.banned ?? false;
        
        this.save = jest.fn().mockImplementation(() => {
            const index = mockedUsers.findIndex(u => u._id === obj._id);
            if (index !== -1) {
                mockedUsers[index] = {
                ...mockedUsers[index],
                ...obj,
                };
            } else {
                mockedUsers.push({
                        _id: obj._id,
                        email: obj.email,
                        displayName: obj.displayName,
                        admin: obj.admin ?? false,
                        registered: obj.registered,
                        picture: obj.picture,
                        learningPreference: obj.learningPreference,
                        interestedLanguages: obj.interestedLanguages,
                        proficientLanguages: obj.proficientLanguages,
                        banned: obj.banned,
                    });
            }
            return { success: true, message: "User saved successfully" };
        });
    }
}

User.find = jest.fn((query) => {
    if (!query) return { exec: () => mockedUsers };
    const { $and, learningPreference, interestedLanguages, proficientLanguages, banned } = query;

    const userId = $and.find((cond) => cond._id && cond._id.$ne)._id.$ne;
    const notInterestUsers = $and.find((cond) => cond._id && cond._id.$nin)._id.$nin;

    let filteredUsers = mockedUsers.filter((user) => {
        return (
            user._id !== userId &&
            !notInterestUsers.includes(user._id) &&
            learningPreference.$in.includes(user.learningPreference) &&
            user.interestedLanguages.some((lang) => interestedLanguages.$in.includes(lang)) &&  
            user.proficientLanguages.some((lang) => proficientLanguages.$in.includes(lang)) &&
            !user.banned
        );
    });
    return { exec: () => filteredUsers}
})

User.findById = jest.fn().mockImplementation((id) =>{ 
    if(id === "errorId"){
        throw new Error('User not found');
    }
    let user = mockedUsers.find((user) => user._id.equals(id));
    return user;
})

User.findOne = jest.fn().mockImplementation((query) =>{
    const { email } = query;
    let user = mockedUsers.find((user) => user.email === email) ?? null;
    return user;
})

  
module.exports = { User };
  