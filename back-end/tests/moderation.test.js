const request = require('supertest');
const app = require('../app'); 

jest.mock('../models/user');
jest.mock('../models/report');

const { mockedUsers } = require('../models/__mocks__/user');    
const { mockedReports } = require('../models/__mocks__/mockedReports');
const { ObjectId } = require('mongodb');

describe('GET /moderation/:adminId', () => {

    // Input: A valid adminId
    // Expected status code: 200
    // Expected behaviour: Returns the reports
    // Expected output: { success: true, reports: mockedReports }
    // ChatGPT Usage: No
    test('Valid adminId', async () => {
        const response = await request(app).get(`/moderation/${mockedUsers[0]._id}`);
        expect(response.statusCode).toBe(200);
        const receivedReports = response.body;
        // compare each received report with the expected report
        for (let i = 0; i < receivedReports.reports.length; i++) {
            expect(receivedReports.reports[i]).toEqual(
                { 
                    ...mockedReports[i],
                    _id: mockedReports[i]._id.toString(),
                    reporterUserId: mockedReports[i].reporterUserId.toString(),
                    reportedUserId: mockedReports[i].reportedUserId.toString(),
                    chatRoomId: mockedReports[i].chatRoomId.toString()
                });
        }
    });


    // Input: An invalid adminId (one not in the database)
    // Expected status code: 400
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Unauthorized access to admin actions' }
    // ChatGPT Usage: No
    test('Invalid adminId', async () => {
        const response = await request(app).get('/moderation/invalidId');
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({success: false, error: 'Unauthorized access to admin actions'});
    });


    // Input: An adminId that will throw error when searching for admin in the database (hardcoded to throw an error in our mock)
    // Expected status code: 500
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Error getting reports' }
    // ChatGPT Usage: No
    test('Get a database error', async () => {
        const response = await request(app).get(`/moderation/errorId`);
        expect(response.statusCode).toBe(500);
        expect(response.body).toEqual({success: false, error: 'Error getting reports'});
    });
});

describe('POST /moderation/', () => {
    
    // Input: A valid report
    // Expected status code: 200
    // Expected behaviour: Returns success message
    // Expected output: { success: true }
    // ChatGPT Usage: No
    test('Valid report', async () => {
        const response = await request(app).post('/moderation/').send(
            {
                reporterUserId: mockedUsers[3]._id,
                reportedUserId: mockedUsers[5]._id,
                chatRoomId: new ObjectId('5f9d88b9d4b4d4c6a0b0f7f9'),
                reportMessage: 'Literally jail them',
            }
        );
        //expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({success: true, message: "Report saved successfully"});
    });

    // Input: An invalid report (missing fields)
    // Expected status code: 400
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Invalid report' }
    // ChatGPT Usage: No
    test('Malformed report', async () => {
        const response = await request(app).post('/moderation').send({});
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({success: false, error: "Invalid report"});
    });


    // Input: An invalid report with invalid reporterUserId
    // Expected status code: 400
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Invalid user ids' }
    // ChatGPT Usage: No
    test('Invalid reporterUserId', async () => {
        const response = await request(app).post('/moderation').send(
            {
                reporterUserId: 'invalidId',
                reportedUserId: mockedUsers[3]._id,
                chatRoomId: new ObjectId('5f9d88b9d4b4d4c6a0b0f7f9'),
                reportMessage: 'Why be mean?',
            }
        );
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({success: false, error: "Invalid report"});
    });


    // Input: An invalid report with invalid reportedUserId
    // Expected status code: 400
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Invalid user ids' }
    // ChatGPT Usage: No
    test('Invalid reportedUserId', async () => {
        const response = await request(app).post('/moderation').send(
            {
                reporterUserId: mockedUsers[3]._id,
                reportedUserId: 'invalidId',
                chatRoomId: new ObjectId('5f9d88b9d4b4d4c6a0b0f7f9'),
                reportMessage: 'Why be mean?',
            }
        );
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({success: false, error: "Invalid report"});
    });


    // Input: A report that will throw error when saving to database (hardcoded to throw an error in our mock)
    // Expected status code: 500
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Error while adding a report' }
    // ChatGPT Usage: No
    test('Get a database error', async () => {
        const response = await request(app).post('/moderation').send(
            {
                reporterUserId: 'errorId',
                reportedUserId: mockedUsers[3]._id,
                chatRoomId: new ObjectId('5f9d88b9d4b4d4c6a0b0f7f9'),
                reportMessage: 'Why be mean?',
            }
        );
        expect(response.statusCode).toBe(500);
        expect(response.body).toEqual({success: false, error: "Error while adding a report"});
    });

});

describe('PUT /moderation/:adminId/ban', () => {

    // Input: A valid adminId, a valid userId, valid reportId
    // Expected status code: 200
    // Expected behaviour: Returns success message
    // Expected output: { success: true }
    // ChatGPT Usage: No
    test('Valid ids', async () => {
        const response = await request(app).put(`/moderation/${mockedUsers[0]._id}/ban`).send(
            { 
                userId: mockedUsers[2]._id,
                reportId: mockedReports[0]._id
            }
        );
        //expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({success: true, message: "User banned successfully"});
    });

    // Input: An invalid adminId (one not in the database), a valid userId, valid reportId
    // Expected status code: 400
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Unauthorized access to admin actions' }
    // ChatGPT Usage: No
    test('Invalid adminId', async () => {
        const response = await request(app).put(`/moderation/invalidId/ban`).send(
            {
                userId: mockedUsers[2]._id,
                reportId: mockedReports[0]._id
            }
        );
        //expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({success: false, error: "Unauthorized access to admin actions"});
    });

    // Input: An adminId that will throw error when searching for admin in the database (hardcoded to throw an error in our mock)
    // Expected status code: 500
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Error while banning user' }
    // ChatGPT Usage: No
    test('Get a database error', async () => {
        const response = await request(app).put(`/moderation/errorId/ban`).send(
            {
                userId: mockedUsers[2]._id,
                reportId: mockedReports[0]._id
            }
        );
        expect(response.statusCode).toBe(500);
        expect(response.body).toEqual({success: false, error: "Error while banning user"});
    });

    // Input: A valid adminId, an invalid userId (one not in the database), valid reportId
    // Expected status code: 400
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Invalid user id' }
    // ChatGPT Usage: No
    test('Invalid userId', async () => {
        const response = await request(app).put(`/moderation/${mockedUsers[0]._id}/ban`).send(
            {
                userId: 'invalidId',
                reportId: mockedReports[0]._id
            }
        );
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({success: false, error: "Invalid user id"});
    });

    // Input: A valid adminId, a valid userId, an invalid reportId (one not in the database)
    // Expected status code: 400
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Report not found' }
    // ChatGPT Usage: No
    test('Invalid reportId', async () => {
        const response = await request(app).put(`/moderation/${mockedUsers[0]._id}/ban`).send(
            {
                userId: mockedUsers[3]._id,
                reportId: 'invalidId'
            }
        );
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({success: false, error: "Report not found"});
    });

    // Input: A valid adminId, a valid userId, a reportId that will throw error when searching for report in the database (hardcoded to throw an error in our mock)
    // Expected status code: 500
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Error while banning user' }
    // ChatGPT Usage: No
    test('Get a database error from report', async () => {
        const response = await request(app).put(`/moderation/${mockedUsers[0]._id}/ban`).send(
            {
                userId: mockedUsers[2]._id,
                reportId: 'errorId'
            }
        );
        expect(response.statusCode).toBe(500);
        expect(response.body).toEqual({success: false, error: "Error while banning user"});
    });

    // Input: A valid adminId, a userId that will throw error when searching for user in the database (hardcoded to throw an error in our mock)
    // Expected status code: 500
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Error while banning user' }
    // ChatGPT Usage: No
    test('Get a database error from user', async () => {
        const response = await request(app).put(`/moderation/${mockedUsers[0]._id}/ban`).send(
            {
                userId: 'errorId',
                reportId: mockedReports[0]._id
            }
        );
        expect(response.statusCode).toBe(500);
        expect(response.body).toEqual({success: false, error: "Error while banning user"});
    });
});

describe('DELETE /moderation/:adminId/:reportId', () => {


    // Input: A valid adminId, a valid reportId
    // Expected status code: 200
    // Expected behaviour: Returns success message
    // Expected output: { success: true, message: 'Report deleted successfully' }
    // ChatGPT Usage: No
    test('Valid ids', async () => {
        const response = await request(app).delete(`/moderation/${mockedUsers[0]._id}/${mockedReports[0]._id}`);
        expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({success: true, message: 'Report deleted successfully'});
    });


    // Input: An invalid adminId (one not in the database), a valid reportId
    // Expected status code: 403
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Unauthorized access to admin actions' }
    // ChatGPT Usage: No
    test('Invalid adminId', async () => {
        const response = await request(app).delete(`/moderation/invalidId/${mockedReports[1]._id}`);
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({success: false, error: 'Unauthorized access to admin actions'});
    });


    // Input: An adminId that will throw error when searching for admin in the database (hardcoded to throw an error in our mock)
    // Expected status code: 500
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Error while deleting report' }
    // ChatGPT Usage: No
    test('Get a database error from admin', async () => {
        const response = await request(app).delete(`/moderation/errorId/${mockedReports[1]._id}`);
        expect(response.statusCode).toBe(500);
        expect(response.body).toEqual({success: false, error: 'Error while deleting report'});
    });


    // Input: A valid adminId, an invalid reportId (one not in the database)
    // Expected status code: 400
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Report not found' }
    // ChatGPT Usage: No
    test('Invalid reportId', async () => {
        const response = await request(app).delete(`/moderation/${mockedUsers[0]._id}/invalidId`);
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({success: false, error: 'Report not found'});
    });


    // Input: A valid adminId, a reportId that will throw error when searching for report in the database (hardcoded to throw an error in our mock)
    // Expected status code: 500
    // Expected behaviour: Returns an error message
    // Expected output: { success: false, error: 'Error while deleting report' }
    // ChatGPT Usage: No
    test('Get a database error from report', async () => {
        const response = await request(app).delete(`/moderation/${mockedUsers[0]._id}/errorId`);
        expect(response.statusCode).toBe(500);
        expect(response.body).toEqual({success: false, error: 'Error while deleting report'});
    });
});