var express = require("express");
const agoraTokenRoutes = require('./routes/agoraTokenRoutes');

var app = express();

app.use(express.json());

app.get("/", (req, res) => {
    res.send("LangSync");
})

//Routes
app.use('/agoraToken', agoraTokenRoutes);

async function run(){
    app.listen(8081)
}

run()