const { google } = require('googleapis');
const moment = require('moment');
const { getGoogleClient } = require('./authenticationService');
const { findUserByID } = require('./userService');

//ChatGPT Usage: No
async function createEvent(authCode, rawEvent) {
    const clientResponse = await getGoogleClient(authCode);

    if(!clientResponse.success) {
        return clientResponse;
    }

    const auth = clientResponse.auth;
    const eventResponse = await generateLangSyncEventObject(rawEvent);

    if(!eventResponse.success) {
        return { success: false, error: 'Error creating event' };
    }

    const event = eventResponse.event;

    try {
        const calendar = google.calendar({ version: 'v3', auth });
        const response = await calendar.events.insert({
            calendarId: 'primary',
            resource: event,
            sendNotifications: true,
        });
        console.log(`Event created: ${response.data.htmlLink}`);
        return { success: true, message: `Event created: ${response.data.htmlLink}` };
    } catch (error) {
        console.error('Error creating event:', error);
        return { success: false, error: 'Error creating event' };
    }
}

//ChatGPT Usage: No
async function generateLangSyncEventObject(rawEvent) {
    const hostUser = await findUserByID(rawEvent.hostUserId);
    const invitedUser = await findUserByID(rawEvent.invitedUserId);

    if(!hostUser || !invitedUser) {
        return { success: false, error: 'Error finding host or invited user' };
    }

    const timeZone = rawEvent.timeZone;
    const startTime = new Date(new Date(rawEvent.startTime));
    const durationMinutes = rawEvent.durationMinutes;
    const endTime = await moment(startTime).add(durationMinutes, 'm').toDate();

    const event = {
        summary: 'LangSync Learning Session',
        location: 'LangSync App',
        description: 'A LangSync Learning Session between ' + hostUser.displayName + ' and ' + invitedUser.displayName,
        attendees: [
            { email: invitedUser.email },
        ],
        end: {
            dateTime: endTime.toISOString(),
            timeZone: timeZone,
        },
        start: {
            dateTime: startTime.toISOString(),
            timeZone: timeZone,
        },    
        reminders: {
            useDefault: true,
        }
    }

    return { success: true, event: event };
}

module.exports = { createEvent };