var express = require("express");
require('dotenv').config()
const { default: mongoose } = require("mongoose");
var app = express();

// Reference: https://socket.io/get-started/chat
const http = require('http');
const https = require('https'); // Add this line
const fs = require('fs'); // Add this line
const server = http.createServer(app);

const options = {
  key: fs.readFileSync('privkey.pem'), // Replace with your private key file path
  cert: fs.readFileSync('fullchain.pem'), // Replace with your certificate file path
};

const secureServer = https.createServer(options, app); // Create an HTTPS server

const { Server } = require("socket.io");
const io = new Server(secureServer); // Use the secure server for Socket.IO

const agoraTokenRoutes = require('./routes/agoraTokenRoutes');
const usersRoutes = require('./routes/usersRoutes');
const authenticationRoutes = require('./routes/authenticationRoutes');
const googleCalendarRoutes = require('./routes/googleCalendarRoutes');
const matchingRoutes = require('./routes/matchingRoutes');
const communicationRoutes = require('./routes/communicationRoutes');

app.use(express.json());

app.get("/", (req, res) => {
    res.send("LangSync");
})

io.on('connection', (socket) => {
    console.log('a user connected');

    socket.on('joinChatroom', (roomId, userId) => {
        socket.userId = userId
        socket.join(roomId);
    });

    socket.on('leaveChatroom', (roomId) => {
        socket.leave(roomId);
    })

    socket.on('sendMessage', (roomId, message) => {
        const userId = socket.userId
        io.to(roomId).emit('message', { userId, message });
    });

    socket.on('disconnect', () => {
        console.log('A user disconnected.');
    });
});

// Routes
app.use('/agoraToken', agoraTokenRoutes);
app.use("/users", usersRoutes);
app.use("/authentication", authenticationRoutes)
app.use("/events", googleCalendarRoutes);
app.use("/matches", matchingRoutes)
app.use("/chatrooms", communicationRoutes)

async function run() {
    try {
        await mongoose.connect(process.env.DATABASE_URL);
        console.log("Connected to the database");
        var server = app.listen(8081, (req, res) => { // Use port 80 for HTTP
            var host = server.address().address;
            var port = server.address().port;
            console.log("Server successfully running at http://%s:%s", host, port);
        });

        secureServer.listen(443, () => { // Use port 443 for HTTPS
            var host = secureServer.address().address;
            var port = secureServer.address().port;
            console.log("Secure server successfully running at https://%s:%s", host, port);
        });
    } catch (err) {
        console.log(err);
        await mongoose.close();
    }
}

run();
