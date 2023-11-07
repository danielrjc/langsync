require('dotenv').config();
const moderationService = require('../services/moderationService');

// ChatGPT Usage: No
// Route for users to add a report
exports.addReport = async (req, res) => {
    console.log(req.body);
    try {
        await moderationService.addReport(req.body);
        return res.status(200).json({ success: true });
    } catch (error) {
        return res.status(500).json({ success: false, error: "Error while adding a report" });
    }
}

// ChatGPT Usage: No
// Route for admins to get all reports
exports.getReports = async (req, res) => {
    let reports;
    try {
        const adminId = req.params.adminId;
        if (moderationService.isAdmin(adminId)) {
            reports = await moderationService.getReports();
            console.log(reports);
            return res.status(200).json({ success: true, reports });
        } else {
            return res.status(403).json({ success: false, message: "Unauthorized access to admin actions" })
        }
    } catch (err) {
        return res.status(500).json({ success: false, error: "Error while fetching reports: " + err });
    }
}

// ChatGPT Usage: No
// Route for admins to delete a report
exports.deleteReport = async (req, res) => {
    console.log(req.body);
    try {
        const adminId = req.params.adminId;
        if (moderationService.isAdmin(adminId)) {
            const reportId = req.params.reportId;
            await moderationService.deleteReport(reportId);

            return res.status(200).json({ success: true });
        } else {
            return res.status(403).json({ success: false, message: "Unauthorized access to admin actions" })
        }
    } catch (error) {
        return res.status(500).json({ success: false, error: "Error while deleting report" + error });
    }
}

// ChatGPT Usage: No
// Route for admins to ban a user
exports.banUser = async (req, res) => {
    console.log("Banned body: " + req.body.userId);
    try {
        const adminId = req.params.adminId;
        if (moderationService.isAdmin(adminId)) {
            const userId = req.body.userId;
            const reportId = req.body.reportId;

            await moderationService.ban(userId);
            await moderationService.deleteReport(reportId);
            
            return res.status(200).json({ success: true });
        } else {
            return res.status(403).json({ success: false, message: "Unauthorized access to admin actions" })
        }
    } catch (error) {
        return res.status(500).json({ success: false, error: "Error while banning user" });
    }
}
