var express = require("express");
var router = express.Router();
var usersController = require("../controllers/usersController");


// Updates the preferences for a user
router.put("/:id/prefs", usersController.updateUserProfile);
// Returns a user's data
router.get("/:id", usersController.getUser);


module.exports = router;
