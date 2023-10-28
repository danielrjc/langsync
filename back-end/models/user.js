const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    userID: {
        type: mongoose.Schema.Types.ObjectId,
        index: true,
        required: true,
        auto: true,
    },
    age: {
        type: Number,
        required: true
    },
    displayName: {
        type: String,
        required: true
    },
    registered:{
        type: Boolean,
        required: true
    },
    picture: {
        type: String,
        required: true
    },
    email: {
        type: String,
        required: true,
        unique: true
    },
    proficientLanguages: {
        type: [String],
        required: true
    },
    interestedLanguages: {
        type: [String],
        required: true
    },
    learningPreference: {
        type: String,
        enum: ['Expert', 'Partner', 'Both'],
        required: true
    },
    matchedUsers: {
        type: [Number],
        required: true
    },
    blockedUsers: {
        type: [Number],
        required: true
    },
    likedUsers: {
        type: [Number],
        required: true
    },
    chatroomIDs: {
        type: [Number],
        required: true
    },
    badges: {
        type: [Number],
        required: true
    },
    interests: {
        business: {
            type: Boolean,
            required: true
        },
        sports: {
            type: Boolean,
            required: true
        },
        cooking: {
            type: Boolean,
            required: true
        },
        travel: {
            type: Boolean,
            required: true
        },
        movies: {
            type: Boolean,
            required: true
        },
        art: {
            type: Boolean,
            required: true
        },
        music: {
            type: Boolean,
            required: true
        }
    },
    desiredUser: {
        age: {
            type: Number,
            required: true
        },
        interests: {
            business: {
                type: Number,
                required: true
            },
            sports: {
                type: Number,
                required: true
            },
            cooking: {
                type: Number,
                required: true
            },
            travel: {
                type: Number,
                required: true
            },
            movies: {
                type: Number,
                required: true
            },
            art: {
                type: Number,
                required: true
            },
            music: {
                type: Number,
                required: true
            }
        },
        languagePreferences: {
            expert: {
                type: Number,
                required: true
            },
            partner: {
                type: Number,
                required: true
            }
        }
    }
});

function getDefaultUser( email, displayName, picture) {
    return new User({
        age: 0,
        displayName: displayName,
        registered: false,
        picture: picture,
        email: email,
        registered: true,
        proficientLanguages: [],
        interestedLanguages: [],
        learningPreference: 'Both',
        likedUsers: [],
        chatroomIDs: [],
        badges: [],
        interests: {
            business: false,
            sports: false,
            cooking: false,
            travel: false,
            movies: false,
            art: false,
            music: false
        },
        desiredUser: {
            age: 0,
            interests: {
                business: 0,
                sports: 0,
                cooking: 0,
                travel: 0,
                movies: 0,
                art: 0,
                music: 0
            },
            languagePreferences: {
                expert: 0,
                partner: 0
            }
        }
    });
}


const User = mongoose.model('User', userSchema);

module.exports = {User, getDefaultUser};
