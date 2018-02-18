# Ticket-Validation-
An Android app to scan qr code containing ticket information and authenticating that information against the one present in MongoDB database.
## This App does the following things.
* It Scans the QRCode and obtains an objectID of the ticket. 
* It makes a GET request through an API to a mongoDB server and checks for the existence of a ticket.
* If the ticket exists then it performs validation of that ticket by checking it against the source and destination stations,journey type and also against the
expiry date of the ticket.
* If validation is successful then it makes a PUT request to the server and sets the flags related to checking and validation. viz(isTicketChecked and isTicketValidated)
* It also sends an sms to the ticket user regarding the validation of ticket.
* If validation is unsuccessful then it just sets the isTicketChecked flag true.
