require('dotenv').config()
const {User, getDefaultUser} = require('../models/user');

async function findUnregistredOrCreateUser(ticket) {
    const payload = ticket.getPayload();
    const email = payload.email;
    const picture = payload.picture;
    const displayName = payload.name;

    let user = await User.findOne({ email: email });
    console.log(user);

    if (!user){
        try {
            user = getDefaultUser(email,displayName, picture);
            await user.save();
            return {success: true, user: user};
        } catch (error) {
            return {success: false, error: error};
        }
    }else if (user.registered === false){
        return {success: false, user: user};
    }
    else{
        return {success: false, error: "User already registered"};
    }
}

async function findUserByEmail(email) {
    console.log("Find user by email " + email)
    const user = await User.findOne({ email: email });
    if (!user || user.registered === false) {
        return {success: false, error: 'User not registered'};
    }

    return {success: true, user: user};
}

module.exports = { findUnregistredOrCreateUser, findUserByEmail };