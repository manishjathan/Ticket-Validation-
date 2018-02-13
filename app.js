const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const mongoose = require('mongoose');

app.use(express.static(__dirname+'/client'));
app.use(bodyParser.json());

var port = process.env.PORT || 3000;
Ticket =require('./models/ticket');

// Connect to Mongoose
mongoose.connect('mongodb://manishjathan22:Manish_1@ds233218.mlab.com:33218/ticketinfo');
var db = mongoose.connection;

app.get('/', (req, res) => {
	res.send('Please use /api/tickets');
});

app.get('/api/tickets/:_id', (req, res) => {
	Ticket.getTicketById(req.params._id, (err, ticket) => {
		if(err){
			throw err;
		}
		res.json(ticket);
	});
});


app.get('/api/tickets', (req, res) => {
	Ticket.getTickets((err, tickets) => {
		if(err){
			throw err;
		}
		res.json(tickets);
	});
});

app.post('/api/tickets', (req, res) => {
	var ticket = req.body;
	Ticket.addTicket(ticket, (err, ticket) => {
		if(err){
			throw err;
		}
		res.json(ticket);
	});
});

app.put('/api/tickets/:_id', (req, res) => {
	var id = req.params._id;
	var ticket = req.body;
	Ticket.updateTicket(id, ticket, {}, (err, ticket) => {
		if(err){
			throw err;
		}
		res.json(ticket);
	});
});

app.delete('/api/tickets/:_id', (req, res) => {
	var id = req.params._id;
	Ticket.removeTicket(id, (err, ticket) => {
		if(err){
			throw err;
		}
		res.json(ticket);
	});
});



app.listen(port);
console.log('Running on port 3000');
