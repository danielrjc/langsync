const { User } = require("../models/user");

const userService = require("./userService")
const communicationService = require("./communicationService")

// ChatGPT Usage: No
// Check if targetUser likes sourceUser and matches
async function createMatch(sourceUserId, targetUserId){
    try{
        const sourceUser = await userService.findUserByID(sourceUserId)
        const targetUser = await userService.findUserByID(targetUserId)
        
        if(!sourceUser || !targetUser || sourceUser.likedUsers.includes(targetUserId)){
            return false;
        }
    
        sourceUser.likedUsers.push(targetUserId);
        updateIdealMatch(sourceUser)
    
        // Check if target user likes source user and create match
        if(targetUser.likedUsers.includes(sourceUserId)){
            sourceUser.matchedUsers.push(targetUserId);
            targetUser.matchedUsers.push(sourceUserId);
            await sourceUser.save();
            await targetUser.save();
            await communicationService.createChatroom(sourceUserId, targetUserId);
            return true;
        }
    
        await sourceUser.save();
        await targetUser.save();
        return false;
    }
    catch(error){
        console.log("Creating match/liking failed: ", error);
    }
}

// ChatGPT Usage: Partial
// Updates the users ideal match through averaging with the last liked user
async function updateIdealMatch(sourceUser){
    try{
        let likedUsersNum = sourceUser.likedUsers.length
        const lastLiked = await User.findById(sourceUser.likedUsers[likedUsersNum - 1])
    
        sourceUser.idealMatch.age = ((sourceUser.idealMatch.age * (likedUsersNum-1)) + lastLiked.age) / (likedUsersNum)
        
        for (const interest in sourceUser.idealMatch.interests) {
            let interestSum = sourceUser.idealMatch.interests[interest] * (likedUsersNum - 1);
            let likedUserInterest = lastLiked.interests[interest] ? 1 : 0;
            interestSum += likedUserInterest
            sourceUser.idealMatch.interests[interest] = interestSum / likedUsersNum;
        }
    
        const lastLikedPref = lastLiked.learningPreference;
    
        // Calculate the new average for learningPreference
        const sourceUserExpertPref = (sourceUser.idealMatch.learningPreference.expert * (likedUsersNum - 1) + (lastLikedPref === 'Both' || lastLikedPref === 'Expert' ? 1 : 0)) / likedUsersNum;
        const sourceUserLearnerPref = (sourceUser.idealMatch.learningPreference.learner * (likedUsersNum - 1) + (lastLikedPref === 'Both' || lastLikedPref === 'Learner' ? 1 : 0)) / likedUsersNum;
        
        // Update the idealMatch learningPreference values
        sourceUser.idealMatch.learningPreference.expert = sourceUserExpertPref;
        sourceUser.idealMatch.learningPreference.learner = sourceUserLearnerPref;
    
        await sourceUser.save()
    
        return true
    }
    catch(error){
        console.log("Updating matches failed: ", error)
    }
}

// ChatGPT Usage: No
// Get the users someone has matches with
async function getAllMatches(userId){
    try{
        const sourceUser = await User.findById(userId);

        return sourceUser.matchedUsers;
    }
    catch(error){
        console.log("Failed to get matched users: ", error)
    }

}

module.exports = {createMatch, getAllMatches};
