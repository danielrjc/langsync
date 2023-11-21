require('dotenv').config()
const {User} = require('../models/user');
const {getDefaultInitialIdealMatch} = require('../models/idealMatch');

// ChatGPT Usage: No
// Find user by email and create if not found
async function findUnregistredOrCreateUser(ticket) {
    const payload = ticket.getPayload();
    const email = payload.email;
    const picture = payload.picture;
    const displayName = payload.name;

    let user = await User.findOne({ email });
    let response;
    if (!user){
        try {
            user = await createUser(email, displayName, picture);
            response = {success: true, user};
            return response;
        } catch (error) {
            response = {success: false, error};
            return response;
        }
    }else if (user.registered === false){
        response = {success: true, user};
        return response;
    }
    else{
        response = {success: false, error: "User already registered"};
        return response;
    }
}

// ChatGPT Usage: No
// Find user by email and return if found
async function findUserByEmail(email) {
    console.log("Find user by email " + email)
    let response;
    const user = await User.findOne({ email });
    if (!user || user.registered === false) {
        response = {success: false, error: 'User not registered'};
        return response;
    } else if (user.banned === true) {
        response = {success: false, error: 'User banned'};
        return response;
    }
    response = {success: true, user};
    return response;
}

// ChatGPT Usage: No
// Find user by ID and return it if found, null otherwise
async function findUserByID(userID) {
    // let user = await User.findOne({ _id : userID });
    let user = await User.findById(userID);

    if (!user) {
        return null;
    }

    return user;
}

// ChatGPT Usage: No
// Find users by filter and return them
async function findUsers(filter){
    let users = await User.find(filter);

    return users;
}

// ChatGPT Usage: No
// Create a new user
async function createUser(email, displayName="", picture="") {
    let userData = {
        email,
        displayName,
        picture,
    }
    let user = new User(userData);
    await user.save();
    return user; 
}

// ChatGPT Usage: No
// Update a user
async function updateUser(userID, userData) {
    let user = await User.findById(userID);

    if (!user) {
        return {success: false, error: "User not found"};
    }

    try {
        user.proficientLanguages = userData.proficientLanguages;
        user.interestedLanguages = userData.interestedLanguages;
        user.learningPreference = userData.learningPreference;
        user.interests = userData.interests;
        user.age = userData.age;

        if (user.registered === false) {
            user.idealMatch = getDefaultInitialIdealMatch(user);
            user.registered = true;
        }

        user.save();

        return {success: true, message: "User updated successfully"};
    } catch (error) {
        return {success: false, error};
    }
}

// ChatGPT Usage: No
// Ban a user
async function banUser(userID) {
    let user = await User.findById(userID);

    if (!user) {
        return {success: false, error: "User not found"};
    }

    try {
        user.banned = true;
        user.save();

        return {success: true, message: "User banned successfully"};
    } catch (error) {
        return {success: false, error};
    }
}

// ChatGPT Usage: No
// Find admin by email and create if not found
async function findAdminOrCreate(email) {

    let admin = await User.findOne({ email });

    if (!admin) { 
        try {
            admin = await createAdmin(email);
        } catch (error) {
            return {success: false, error};
        }
    }
    if (admin.banned === true) {
        return {success: false, error: 'User banned'};
    }

    return {success: true, user: admin};
}

// ChatGPT Usage: No
// Create a new admin
async function createAdmin(email) {
    let adminData = {
        email,
        admin: true,
        registered: true,
        displayName: "Admin",
        picture: ""
    }
    let admin = new User(adminData);
    await admin.save();
    return admin;
}

module.exports = { findUnregistredOrCreateUser, findUserByEmail, findUserByID, findUsers, createUser, updateUser, banUser, findAdminOrCreate, createAdmin };
