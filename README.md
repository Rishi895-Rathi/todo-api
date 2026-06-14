What Problem It Solves ?
*************************************************************************************************
A secure backend system where:

People can register and login with their email/password
Their passwords are never stored as plain text-always hashed
After login they get a JWT token-like a digital ID card
They use that token to access protected routes
Admins can create/update/delete tasks, users can only read them

************************************************************************************************
What I Built- Layer by Layer
*************************************************************************************************
## Database Layer

PostgreSQL database connected
Two tables auto-created by Hibernate:

users- stores registered users with hashed passwords and roles
task- stores tasks with title, description, timestamps


## Security Layer

BCrypt password hashing- even if DB is hacked, passwords are unreadable
JWT Authentication- stateless login system, no sessions needed
Role based access- USER can only read tasks, ADMIN can create/update/delete
Admin secret- special key required to register as admin, prevents anyone from making themselves admin

## API Layer
Five task endpoints under /api/v1/tasks:

GET /tasks- get all tasks
GET /tasks/{id}- get one task
POST /tasks- create task (ADMIN only)
PUT /tasks/{id}- update task (ADMIN only)
DELETE /tasks/{id}- delete task (ADMIN only)

Two auth endpoints under /api/v1/auth:

POST /auth/register- register new user, returns JWT token
POST /auth/login- login, returns JWT token

 ## Error Handling Layer

Every error returns clean JSON instead of ugly HTML
404 when task/user not found
401 when wrong password
403 when wrong admin secret
409 when email already registered
400 when validation fails (blank title etc.)
500 for unexpected errors

## Code Quality

Clean package structure (auth, config, controller, model, repository, security, exception, DTO)
Environment variables via .env-no passwords hardcoded
Timestamps on tasks (createdAt, updatedAt) auto-managed


How a Request Flows
Postman → JwtAuthFilter → SecurityConfig → Controller → Service → Repository → PostgreSQL
For example a login:

You send email + password to /api/v1/auth/login
Spring checks credentials against DB
If correct → generates JWT token with your user ID and role inside
You get the token back
Every future request you send that token in the header
JwtAuthFilter reads the token, verifies it, sets your identity
Spring allows or blocks the request based on your role


What's Still Pending
Link tasks to specific users - done 
Swagger documentation - done
Docker + deployment - partially
