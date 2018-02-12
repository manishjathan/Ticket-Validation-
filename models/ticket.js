const mongoose = require('mongoose');

// Book Schema
const ticketSchema = mongoose.Schema({
	// Defining Model Schema in Mongoose
	Name:{
		type: String,
		required: true
	},
	source:{
		type: String,
		required: true
	},
	destination:{
		type: String,
		required: true
	},
	journeyType:{
		type: String,
		required: true
	},
	fare:{
		type: String,
		required: true
	},
	expiry:{
		type: Date,
		required : true
	},
	isTicketChecked:{
		type: Boolean,
		default : false
	},
	isTicketValidated:{
		type: Boolean,
		default : false
	}
});

const Ticket = module.exports = mongoose.model('Ticket', ticketSchema);

// Get Tickets
module.exports.getTickets = (callback, limit) => {
	Ticket.find(callback).limit(limit);
}

// Get Ticket
module.exports.getTicketById = (id, callback) => {
	Ticket.findById(id, callback);
}

// Add Ticket
module.exports.addTicket = (ticket, callback) => {
	Ticket.create(ticket, callback);
}

// Update Book
module.exports.updateTicket = (id, ticket, options, callback) => {
	var query = {_id: id};
	var update = {
		Name: ticket.Name,
		source: ticket.source,
		destination: ticket.destination,
		fare: ticket.fare,
		journeyType: ticket.journeyType,
		expiry: ticket.expiry,
		isTicketChecked: ticket.isTicketChecked,
		isTicketValidated: ticket.isTicketValidated
	}
	Ticket.findOneAndUpdate(query, update, options, callback);
}

// Delete Book
module.exports.removeBook = (id, callback) => {
	var query = {_id: id};
	Ticket.remove(query, callback);
}
