# User Profile API Setup Guide

## Quick Setup

### 1. Configure Supabase Credentials

Update `src/main/resources/application.properties` with your Supabase credentials:

```properties
# Supabase Configuration
supabase.url=https://your-project.supabase.co
supabase.key=your_anon_key_here
supabase.service-role-key=your_service_role_key_here
```

You can find these values in your Supabase Dashboard:
- Go to Settings → API
- Copy the Project URL and anon public key

### 2. Build and Run

```bash
./gradlew build
./gradlew bootRun
```

### 3. Test the API

The API will be available at `http://localhost:8080`

## API Endpoints

### User Profile Endpoints

- **GET** `/api/users` - Get all users
- **GET** `/api/users/{id}` - Get user by ID
- **GET** `/api/users/email/{email}` - Get user by email
- **GET** `/api/users/google/{googleId}` - Get user by Google ID
- **GET** `/api/users/search?username=value` - Search users by username
- **POST** `/api/users` - Create new user
- **PUT** `/api/users/{id}` - Update user
- **PATCH** `/api/users/{id}/login` - Update last login
- **DELETE** `/api/users/{id}` - Delete user

### Test Endpoints

- **GET** `/` - Welcome message
- **GET** `/supabase/test` - Test Supabase connection
- **GET** `/test/users` - Test user service

## Example Usage

### Create a User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "google_id": "123456789",
    "name": "John Doe",
    "first_name": "John",
    "last_name": "Doe",
    "username": "johndoe"
  }'
```

### Get All Users
```bash
curl http://localhost:8080/api/users
```

### Get User by Email
```bash
curl http://localhost:8080/api/users/email/user@example.com
```

## Database Schema

The API works with the `user_profiles` table you created in Supabase with the following fields:

- `id` (UUID, Primary Key)
- `email` (TEXT, Unique)
- `google_id` (TEXT, Unique)
- `name` (TEXT)
- `first_name` (TEXT)
- `last_name` (TEXT)
- `profile_picture_url` (TEXT)
- `username` (TEXT, Unique)
- `bio` (TEXT)
- `obrobucks` (INTEGER, Default: 0)
- `access_token` (TEXT)
- `refresh_token` (TEXT)
- `token_expires_at` (TIMESTAMP WITH TIME ZONE)
- `last_login` (TIMESTAMP WITH TIME ZONE)
- `created_at` (TIMESTAMP WITH TIME ZONE)
- `updated_at` (TIMESTAMP WITH TIME ZONE)

## Frontend Integration

This API is ready to be consumed by your frontend application. The endpoints return JSON and accept JSON payloads, making it easy to integrate with React, Vue, Angular, or any other frontend framework.

Example frontend fetch:
```javascript
// Get all users
const response = await fetch('http://localhost:8080/api/users');
const users = await response.json();

// Create a user
const newUser = await fetch('http://localhost:8080/api/users', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'user@example.com',
    name: 'John Doe',
    username: 'johndoe'
  })
});
```

## Next Steps

1. Configure your Supabase credentials
2. Test the API endpoints
3. Integrate with your frontend
4. Add authentication middleware if needed
5. Configure CORS for your frontend domain in production