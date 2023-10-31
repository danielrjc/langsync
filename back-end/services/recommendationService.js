const { User } = require("../models/user");
const { getDefaultInitialIdealMatch } = require("../models/idealMatch");


// Check if targetUser likes sourceUser and matches
//ChatGPT Usage: No
async function getRecommendedUsers(userId){
    let user = await User.findById(userId);
    // TODO: Add a filter to the find function to 
    // Filter users with  valid learning preferences/languages
    // Filter not registered users
    // Filter users that have been blocked by the user
    // Filter users that have already been liked by the user
    // Filtereing liked should filter matchs as well
    let users = await User.find({});

    return users;
    const arbitraryTopUsers = [];
    const scoredUsers = [];
    

    for (let i = 0; i < users.length; i++) {
        let targetUser = users[i];
        if (targetUser._id == userId ) {
            continue;
        }
        if (targetUser.likedUsers.includes(userId) ) {
            arbitraryTopUsers.push(targetUser);
        } else{
            scoredUsers.push(targetUser);
        }
    }

    const sortedScoredUsers = sortUsersOnScore(scoredUsers, user.idealMatch);
    const recommendedUsers = arbitraryTopUsers.concat(sortedScoredUsers);

    return recommendedUsers;
}
//ChatGPT Usage: No
function sortUsersOnScore (users, idealMatch) {
    const usersWithScore = users.map((user) => {
        const score = getScore(user, idealMatch);
        return { user, score };
    });
    const sortedUsersWithScore = usersWithScore.sort((a, b) => a.score - b.score);
    return sortedUsersWithScore.map((userWithScore) => userWithScore.user);
}

//ChatGPT Usage: No
function getScore(user, idealMatch) {
    const formatedUser = getDefaultInitialIdealMatch(user);
    const score = calculateScore(formatedUser, idealMatch);
    return score;
}

//ChatGPT Usage: Yes
function calculateScore(user, targetUser) {
    // Step 1: Calculate Euclidean distances
    const ageDistance = Math.abs(user.age - targetUser.age);
    
    const learningPreferenceDistance = Math.sqrt(
        Math.pow(user.learningPreference.expert - targetUser.learningPreference.expert, 2) +
        Math.pow(user.learningPreference.partner - targetUser.learningPreference.partner, 2)
    );
    
    const interestsDistance = Math.sqrt(
        Object.keys(user.interests).reduce((acc, interest) => {
            return acc + Math.pow(user.interests[interest] - targetUser.interests[interest], 2);
        }, 0)
    );

    // Step 2: Scale the distances
    const maxAgeDifference = 80; // Maximum age difference (adjust as needed)
    const maxLearningPreferenceDistance = Math.sqrt(2); // Maximum distance for learningPreference
    const maxInterestsDistance = Math.sqrt(Object.keys(user.interests).length); // Maximum distance for interests

    const scaledAge = ageDistance / maxAgeDifference;
    const scaledLearningPreference = learningPreferenceDistance / maxLearningPreferenceDistance;
    const scaledInterests = interestsDistance / maxInterestsDistance;

    // Step 3: Calculate the overall similarity score
    const interestsWeight = 3;
    const similarityScore = Math.sqrt(
        Math.pow(scaledAge, 2) +
        Math.pow(scaledLearningPreference, 2) +
        Math.pow(scaledInterests, 2) * interestsWeight
    );

    return similarityScore;
}

module.exports = {getRecommendedUsers};